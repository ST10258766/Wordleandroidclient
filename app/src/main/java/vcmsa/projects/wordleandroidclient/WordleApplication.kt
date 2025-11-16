package vcmsa.projects.wordleandroidclient

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.wordleandroidclient.api.RetrofitClient
import vcmsa.projects.wordleandroidclient.data.DailyWordRepository
import vcmsa.projects.wordleandroidclient.data.OfflineSyncManager

class WordleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Pre-fetch today's word on app start
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            try {
                val dailyWordRepo = DailyWordRepository(applicationContext, RetrofitClient.wordService)

                Log.d("WordleApp", "Pre-fetching today's word...")
                dailyWordRepo.preFetchTodaysWord()
                Log.d("WordleApp", "✅ Pre-fetch complete")

                // Also try to sync any offline guesses
                val syncManager = OfflineSyncManager(applicationContext, RetrofitClient.wordService)
                Log.d("WordleApp", "Syncing offline guesses...")
                syncManager.syncUnsyncedGuesses()
                Log.d("WordleApp", "✅ Sync complete")

            } catch (e: Exception) {
                Log.e("WordleApp", "Error during initialization: ${e.message}", e)
            }
        }
    }
}