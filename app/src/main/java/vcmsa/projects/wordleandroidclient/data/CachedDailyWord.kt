package vcmsa.projects.wordleandroidclient.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached daily word metadata for offline play
 */
@Entity(tableName = "cached_daily_words")
data class CachedDailyWord(
    @PrimaryKey
    val compositeKey: String,  // "${date}_${lang}"

    val date: String,
    val lang: String,
    val mode: String,
    val length: Int,
    val hasDefinition: Boolean,
    val hasSynonym: Boolean,
    val played: Boolean,
    val answer: String? = null,

    // Cached timestamp
    val cachedAt: Long = System.currentTimeMillis()
)