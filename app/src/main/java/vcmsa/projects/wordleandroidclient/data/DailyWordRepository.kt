package vcmsa.projects.wordleandroidclient.data

import android.content.Context
import android.util.Log
import vcmsa.projects.wordleandroidclient.api.WordApiService
import vcmsa.projects.wordleandroidclient.api.WordTodayResponse
import vcmsa.projects.wordleandroidclient.utils.hasInternetConnection
import java.time.LocalDate

class DailyWordRepository(
    private val context: Context,
    private val wordApi: WordApiService
) {
    private val db = WordleDatabase.getDatabase(context)
    private val cachedWordDao = db.cachedDailyWordDao()

    /**
     * Pre-fetch today's word and cache it
     * Call this when app starts
     */
    suspend fun preFetchTodaysWord(lang: String = "en-ZA"): WordTodayResponse? {
        val today = getTodayDateString()

        // Check if already cached
        val existingCache = cachedWordDao.getCachedWord(today, lang)
        if (existingCache != null) {
            Log.d("DailyWordRepo", "Word already cached for $today, skipping fetch")
            return existingCache.toWordTodayResponse()
        }

        if (!hasInternetConnection(context)) {
            Log.d("DailyWordRepo", "Offline - no cache available")
            return null
        }

        return try {
            Log.d("DailyWordRepo", "Fetching word from API for $today...")
            // Fetch from API
            val response = wordApi.getToday(lang)
            val meta = response.body()

            if (response.isSuccessful && meta != null) {
                // Cache it
                cacheWord(meta)
                Log.d("DailyWordRepo", "✅ Pre-fetched and cached: ${meta.date}, length: ${meta.length}")
                meta
            } else {
                Log.e("DailyWordRepo", "Failed to fetch: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("DailyWordRepo", "Error fetching word: ${e.message}", e)
            null
        }
    }

    /**
     * Get today's word - from cache if available, otherwise fetch
     */
    suspend fun getTodaysWord(lang: String = "en-ZA"): WordTodayResponse? {
        val today = getTodayDateString()

        // Try cache first
        val cached = cachedWordDao.getCachedWord(today, lang)
        if (cached != null) {
            Log.d("DailyWordRepo", "Using cached word for $today")
            return cached.toWordTodayResponse()
        }

        // Not cached, try to fetch
        return preFetchTodaysWord(lang)
    }

    /**
     * Update cached word with answer (from submit response)
     */
    suspend fun updateCachedAnswer(date: String, lang: String, answer: String) {
        val cached = cachedWordDao.getCachedWord(date, lang)
        if (cached != null) {
            val updated = cached.copy(answer = answer)
            cachedWordDao.insertCachedWord(updated)
            Log.d("DailyWordRepo", "✅ Updated cached word with answer for offline play")
        } else {
            Log.w("DailyWordRepo", "Cannot update answer - word not cached yet")
        }
    }

    /**
     * Load word from cache
     */
    private suspend fun loadFromCache(date: String, lang: String): WordTodayResponse? {
        val cached = cachedWordDao.getCachedWord(date, lang)
        return cached?.toWordTodayResponse()
    }

    /**
     * Cache a word
     */
    private suspend fun cacheWord(meta: WordTodayResponse) {
        val cached = CachedDailyWord(
            compositeKey = "${meta.date}_${meta.lang}",
            date = meta.date,
            lang = meta.lang,
            mode = meta.mode,
            length = meta.length,
            hasDefinition = meta.hasDefinition,
            hasSynonym = meta.hasSynonym,
            played = meta.played,
            answer = meta.answer?.uppercase()
        )
        cachedWordDao.insertCachedWord(cached)
        Log.d("DailyWordRepo", "Cached word with answer: ${meta.answer}")
    }

    /**
     * Clean up old cached words (optional, for housekeeping)
     */
    suspend fun cleanupOldCache(daysToKeep: Int = 7) {
        val cutoffTimestamp = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        cachedWordDao.deleteOldCache(cutoffTimestamp)
    }

    private fun getTodayDateString(): String {
        return LocalDate.now().toString()
    }
}

// Extension function to convert cached entity to API response
private fun CachedDailyWord.toWordTodayResponse(): WordTodayResponse {
    return WordTodayResponse(
        date = date,
        lang = lang,
        mode = mode,
        length = length,
        hasDefinition = hasDefinition,
        hasSynonym = hasSynonym,
        played = played
    )
}