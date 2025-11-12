package vcmsa.projects.wordleandroidclient.data



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert
    suspend fun insertWord(word: Word)

    @Query("SELECT * FROM words ORDER BY timestamp DESC")
    fun getAllWords(): Flow<List<Word>>

    @Query("DELETE FROM words")
    suspend fun deleteAll()
}
