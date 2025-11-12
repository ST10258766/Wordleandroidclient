package vcmsa.projects.wordleandroidclient
import androidx.room.*

@Entity(tableName = "game_data")
data class GameData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String?,
    val score: Int,
    val streak: Int,
    val date: Long = System.currentTimeMillis(),
    val synced: Boolean = false // false = not uploaded yet
)

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameData)

    @Query("SELECT * FROM game_data WHERE synced = 0")
    suspend fun getUnsyncedGames(): List<GameData>

    @Update
    suspend fun updateGame(game: GameData)
}

@Database(entities = [GameData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}