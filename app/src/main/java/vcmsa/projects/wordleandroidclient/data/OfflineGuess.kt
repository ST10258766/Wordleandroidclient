package vcmsa.projects.wordleandroidclient.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "offline_guesses")
@TypeConverters(Converters::class)
data class OfflineGuess(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String,
    val date: String,              // e.g., "2025-10-06"
    val lang: String,              // e.g., "en-ZA"
    val guess: String,             // e.g., "CRANE"
    val feedback: List<String>,    // ["G", "Y", "A", "A", "G"]
    val rowIndex: Int,             // Which row (0-5)
    val won: Boolean,              // True if this guess won the game
    val timestamp: Long,           // When guess was made
    val synced: Boolean = false    // Whether synced to server
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}