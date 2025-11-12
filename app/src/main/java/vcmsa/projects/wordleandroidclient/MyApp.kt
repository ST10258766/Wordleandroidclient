package vcmsa.projects.wordleandroidclient

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import vcmsa.projects.wordleandroidclient.data.SettingsStore
import vcmsa.projects.wordleandroidclient.data.WordDatabase
import vcmsa.projects.wordleandroidclient.data.SyncManager
import vcmsa.projects.wordleandroidclient.data.WordRepository


class MyApp : Application() {
    // Lazily initialize the Room database
    val database: WordDatabase by lazy { WordDatabase.getDatabase(this) }
    lateinit var repository: WordRepository


    override fun onCreate() {
        super.onCreate()

        // Existing theme setup ✅
        val isDark = runBlocking { SettingsStore.darkThemeFlow(this@MyApp).first() }
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        // Initialize repository
        repository = WordRepository(database.wordDao())

        // Trigger sync (replace "userId" with current Firebase user ID)
        val userId = "CURRENT_USER_ID"
        SyncManager.trySync(this, userId, repository)
    }
}