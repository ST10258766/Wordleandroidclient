package vcmsa.projects.wordleandroidclient.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import vcmsa.projects.wordleandroidclient.api.SubmitDailyRequest
import vcmsa.projects.wordleandroidclient.api.WordApiService
import vcmsa.projects.wordleandroidclient.utils.hasInternetConnection

class OfflineSyncManager(
    private val context: Context,
    private val wordApi: WordApiService
) {
    private val db = WordleDatabase.getDatabase(context)
    private val guessDao = db.offlineGuessDao()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Get current user ID (Firebase or device-based)
     */
    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_${android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )}"
    }

    /**
     * Save a guess locally (works offline)
     */
    suspend fun saveGuessLocally(
        date: String,
        lang: String,
        guess: String,
        feedback: List<String>,
        rowIndex: Int,
        won: Boolean
    ) {
        val userId = getCurrentUserId()

        val offlineGuess = OfflineGuess(
            userId = userId,
            date = date,
            lang = lang,
            guess = guess,
            feedback = feedback,
            rowIndex = rowIndex,
            won = won,
            timestamp = System.currentTimeMillis(),
            synced = false
        )

        guessDao.insertGuess(offlineGuess)
        Log.d("OfflineSync", "Saved guess locally: $guess for $date (user: $userId)")
    }

    /**
     * Load local guesses for a specific date
     */
    suspend fun loadLocalGuesses(date: String, lang: String): List<OfflineGuess> {
        val userId = getCurrentUserId()
        return guessDao.getGuessesForDate(userId, date, lang)
    }

    /**
     * Check if user has COMPLETED today's game (won or used all 6 attempts)
     */
    suspend fun hasCompletedToday(date: String, lang: String): Boolean {
        val userId = getCurrentUserId()
        val guesses = guessDao.getGuessesForDate(userId, date, lang)
        if (guesses.isEmpty()) return false

        // Check if user won
        val hasWon = guesses.any { it.won }
        if (hasWon) return true

        // Check if user used all 6 attempts (rows 0-5)
        val maxRow = guesses.maxOfOrNull { it.rowIndex } ?: -1
        return maxRow >= 5
    }

    /**
     * Check if user has any guesses for today (even incomplete game)
     */
    suspend fun hasAnyGuessesToday(date: String, lang: String): Boolean {
        val userId = getCurrentUserId()
        val count = guessDao.getGuessCount(userId, date, lang)
        return count > 0
    }

    /**
     * Sync all unsynced guesses to backend and Firestore
     */
    suspend fun syncUnsyncedGuesses(): SyncResult {
        if (!hasInternetConnection(context)) {
            Log.d("OfflineSync", "No internet, skipping sync")
            return SyncResult.NoInternet
        }

        val unsyncedGuesses = guessDao.getUnsyncedGuesses()
        if (unsyncedGuesses.isEmpty()) {
            Log.d("OfflineSync", "No unsynced guesses")
            return SyncResult.NothingToSync
        }

        // Group by userId + date + lang
        val groupedGuesses = unsyncedGuesses.groupBy { "${it.userId}_${it.date}_${it.lang}" }

        var syncedCount = 0
        var failedCount = 0

        for ((key, guesses) in groupedGuesses) {
            val userId = guesses.first().userId
            val date = guesses.first().date
            val lang = guesses.first().lang

            try {
                // Sort by row index
                val sortedGuesses = guesses.sortedBy { it.rowIndex }
                val guessStrings = sortedGuesses.map { it.guess }
                val won = sortedGuesses.any { it.won }

                // 1. Submit to backend API
                try {
                    val submitResult = wordApi.submitDaily(
                        SubmitDailyRequest(
                            date = date,
                            lang = lang,
                            guesses = guessStrings,
                            won = won,
                            durationSec = null,
                            clientId = null
                        )
                    )
                    Log.d("OfflineSync", "Backend API sync response: ${submitResult.code()}")
                } catch (e: Exception) {
                    Log.e("OfflineSync", "Backend API sync failed (continuing to Firestore): ${e.message}")
                }

                // 2. Write to Firestore (only if authenticated)
                if (userId.startsWith("anonymous_").not()) {
                    val docId = "${date}_${lang}_${userId}"

                    val feedbackRowsFlattened = sortedGuesses.map { guess ->
                        guess.feedback.joinToString(",")
                    }

                    val payload = hashMapOf(
                        "uid" to userId,
                        "date" to date,
                        "lang" to lang,
                        "mode" to "daily",
                        "guesses" to guessStrings,
                        "feedbackRows" to feedbackRowsFlattened,
                        "won" to won,
                        "guessCount" to guessStrings.size,
                        "durationSec" to 0,
                        "syncedFromOffline" to true,
                        "submittedAt" to FieldValue.serverTimestamp()
                    )

                    firestore.collection("results")
                        .document(docId)
                        .set(payload)
                        .await()

                    Log.d("OfflineSync", "Firestore sync successful for $docId")
                }

                // 3. Mark as synced in local DB
                guessDao.markDateAsSynced(userId, date, lang)

                syncedCount++
                Log.d("OfflineSync", "✅ Synced $date successfully")

            } catch (e: Exception) {
                Log.e("OfflineSync", "❌ Failed to sync $date: ${e.message}", e)
                failedCount++
            }
        }

        return if (failedCount == 0) {
            SyncResult.Success(syncedCount)
        } else {
            SyncResult.PartialSuccess(syncedCount, failedCount)
        }
    }
}

sealed class SyncResult {
    object NoInternet : SyncResult()
    object NothingToSync : SyncResult()
    data class Success(val count: Int) : SyncResult()
    data class PartialSuccess(val synced: Int, val failed: Int) : SyncResult()
}