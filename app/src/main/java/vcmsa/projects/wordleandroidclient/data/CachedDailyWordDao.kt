package vcmsa.projects.wordleandroidclient.data

import androidx.room.*

@Dao
interface CachedDailyWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedWord(word: CachedDailyWord)

    @Query("SELECT * FROM cached_daily_words WHERE date = :date AND lang = :lang LIMIT 1")
    suspend fun getCachedWord(date: String, lang: String): CachedDailyWord?

    @Query("DELETE FROM cached_daily_words WHERE cachedAt < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)

    @Query("SELECT * FROM cached_daily_words ORDER BY cachedAt DESC LIMIT 1")
    suspend fun getLatestCachedWord(): CachedDailyWord?
}