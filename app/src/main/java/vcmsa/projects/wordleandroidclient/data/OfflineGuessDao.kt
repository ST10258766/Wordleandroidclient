package vcmsa.projects.wordleandroidclient.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineGuessDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuess(guess: OfflineGuess): Long

    @Query("SELECT * FROM offline_guesses WHERE userId = :userId AND date = :date AND lang = :lang ORDER BY rowIndex ASC")
    suspend fun getGuessesForDate(userId: String, date: String, lang: String): List<OfflineGuess>

    @Query("SELECT * FROM offline_guesses WHERE userId = :userId AND date = :date AND lang = :lang ORDER BY rowIndex ASC")
    fun getGuessesForDateFlow(userId: String, date: String, lang: String): Flow<List<OfflineGuess>>

    @Query("SELECT * FROM offline_guesses WHERE synced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedGuesses(): List<OfflineGuess>

    @Query("UPDATE offline_guesses SET synced = 1 WHERE userId = :userId AND date = :date AND lang = :lang")
    suspend fun markDateAsSynced(userId: String, date: String, lang: String)

    @Query("DELETE FROM offline_guesses WHERE userId = :userId AND date = :date AND lang = :lang")
    suspend fun deleteGuessesForDate(userId: String, date: String, lang: String)

    @Query("SELECT COUNT(*) FROM offline_guesses WHERE userId = :userId AND date = :date AND lang = :lang")
    suspend fun getGuessCount(userId: String, date: String, lang: String): Int

    @Query("SELECT * FROM offline_guesses WHERE userId = :userId AND date = :date AND lang = :lang AND won = 1 LIMIT 1")
    suspend fun getWinningGuess(userId: String, date: String, lang: String): OfflineGuess?
}