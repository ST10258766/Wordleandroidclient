package vcmsa.projects.wordleandroidclient

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import vcmsa.projects.wordleandroidclient.api.RetrofitClient
import vcmsa.projects.wordleandroidclient.data.DailyWordRepository
import vcmsa.projects.wordleandroidclient.data.OfflineSyncManager
import vcmsa.projects.wordleandroidclient.data.SettingsStore
import vcmsa.projects.wordleandroidclient.notifications.NotificationHelper

class WordleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1️⃣ Apply saved dark / light theme at startup (this replaces MyApp)
        val isDark = runBlocking {
            SettingsStore.darkThemeFlow(this@WordleApplication).first()
        }
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // 2️⃣ Create notification channels once for the whole app
        NotificationHelper.createChannels(this)

        // 3️⃣ Pre-fetch today's word + sync any offline guesses (your original logic)
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            try {
                val dailyWordRepo =
                    DailyWordRepository(applicationContext, RetrofitClient.wordService)

                Log.d("WordleApp", "Pre-fetching today's word...")
                dailyWordRepo.preFetchTodaysWord()
                Log.d("WordleApp", "✅ Pre-fetch complete")

                val syncManager =
                    OfflineSyncManager(applicationContext, RetrofitClient.wordService)
                Log.d("WordleApp", "Syncing offline guesses...")
                syncManager.syncUnsyncedGuesses()
                Log.d("WordleApp", "✅ Sync complete")

            } catch (e: Exception) {
                Log.e("WordleApp", "Error during initialization: ${e.message}", e)
            }
        }
    }
}
