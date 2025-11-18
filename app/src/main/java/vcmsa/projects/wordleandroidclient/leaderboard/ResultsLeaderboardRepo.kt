package vcmsa.projects.wordleandroidclient.leaderboard

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

data class LeaderboardEntry(
    val uid: String = "",
    val username: String? = null,
    val photoUrl: String? = null,
    val guessesUsed: Int = 0,
    val score: Int = 0,
    val timeRemainingSec: Int = 0
)

class ResultsLeaderboardRepo(
    private val db: FirebaseFirestore
) {
    /** Fetch today's Speedle winners sorted by score - ONE entry per user (best score only). */
    suspend fun fetchSpeedleLeaderboard(
        durationSec: Int = 90,
        limit: Long = 50
    ): List<LeaderboardEntry> {
        val today = LocalDate.now().toString()

        Log.d("Leaderboard", "==========================================")
        Log.d("Leaderboard", "Fetching Speedle leaderboard")
        Log.d("Leaderboard", "Date: $today")
        Log.d("Leaderboard", "Duration: $durationSec")

        try {
            val query = db.collection("speedle_sessions")
                .whereEqualTo("date", today)
                .whereEqualTo("durationSec", durationSec)
                .whereEqualTo("won", true)
                .limit(200) // Get more to filter duplicates

            val snaps = query.get().await()
            Log.d("Leaderboard", "Found ${snaps.size()} total sessions")

            // Map to store BEST score per user
            val bestByUser = mutableMapOf<String, LeaderboardEntry>()

            for (doc in snaps.documents) {
                val uid = doc.getString("uid") ?: "unknown"
                val username = doc.getString("username")
                    ?: doc.getString("displayName")
                    ?: "Player"
                val photoUrl = doc.getString("photoUrl")
                val guessesUsed = doc.getLong("guessesUsed")?.toInt() ?: 0
                val score = doc.getLong("score")?.toInt() ?: 0
                val timeRemaining = doc.getLong("timeRemainingSec")?.toInt() ?: 0

                val entry = LeaderboardEntry(
                    uid = uid,
                    username = username,
                    photoUrl = photoUrl,
                    guessesUsed = guessesUsed,
                    score = score,
                    timeRemainingSec = timeRemaining
                )

                // Keep only the BEST score per user
                val existing = bestByUser[uid]
                if (existing == null || entry.score > existing.score) {
                    bestByUser[uid] = entry
                    Log.d("Leaderboard", "Updated best for $username: score=$score")
                }
            }

            // Convert to list and sort by score (descending)
            val sorted = bestByUser.values
                .sortedByDescending { it.score }
                .take(limit.toInt())

            Log.d("Leaderboard", "Final unique users: ${sorted.size}")
            Log.d("Leaderboard", "==========================================")

            return sorted

        } catch (e: Exception) {
            Log.e("Leaderboard", "Error fetching leaderboard", e)
            return emptyList()
        }
    }
}