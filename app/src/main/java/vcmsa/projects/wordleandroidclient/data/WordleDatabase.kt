package vcmsa.projects.wordleandroidclient.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        OfflineGuess::class,
        CachedDailyWord::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WordleDatabase : RoomDatabase() {

    abstract fun offlineGuessDao(): OfflineGuessDao
    abstract fun cachedDailyWordDao(): CachedDailyWordDao

    companion object {
        @Volatile
        private var INSTANCE: WordleDatabase? = null

        fun getDatabase(context: Context): WordleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordleDatabase::class.java,
                    "wordle_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}