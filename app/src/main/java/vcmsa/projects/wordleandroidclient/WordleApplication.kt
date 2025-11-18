package vcmsa.projects.wordleandroidclient

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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

    companion object {
        /**
         * Apply the app's locale based on a code ("en" or "af").
         * If something unexpected comes in, default to English.
         */
        fun applyLanguage(code: String) {
            val tag = when (code.lowercase()) {
                "af" -> "af"
                else -> "en"
            }
            val appLocales = LocaleListCompat.forLanguageTags(tag)
            AppCompatDelegate.setApplicationLocales(appLocales)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 1) Read saved language, or detect from system on first run
        val languageCode = runBlocking {
            SettingsStore.getLanguageCodeOnce(applicationContext)
        }

        val finalCode = if (languageCode.isNullOrBlank()) {
            // First time: auto-detect system language
            val sysLang = resources.configuration.locales[0].language
            val detected = if (sysLang.startsWith("af", ignoreCase = true)) {
                "af"
            } else {
                "en"
            }

            // Save it asynchronously so next launch uses this directly
            ProcessLifecycleOwner.get().lifecycleScope.launch {
                SettingsStore.setLanguageCode(applicationContext, detected)
            }

            detected
        } else {
            languageCode
        }

        // 2) Apply the locale for this app process
        applyLanguage(finalCode)


        //  Apply saved dark / light theme at startup
        val isDark = runBlocking {
            SettingsStore.darkThemeFlow(this@WordleApplication).first()
        }
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Create notification channels once for the whole app
        NotificationHelper.createChannels(this)

        // Pre-fetch today's word + sync any offline guesses
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            try {
                val dailyWordRepo =
                    DailyWordRepository(applicationContext, RetrofitClient.wordService)

                Log.d("WordleApp", "Pre-fetching today's word...")
                dailyWordRepo.preFetchTodaysWord()
                Log.d("WordleApp", " Pre-fetch complete")

                val syncManager =
                    OfflineSyncManager(applicationContext, RetrofitClient.wordService)
                Log.d("WordleApp", "Syncing offline guesses...")
                syncManager.syncUnsyncedGuesses()
                Log.d("WordleApp", "Sync complete")

            } catch (e: Exception) {
                Log.e("WordleApp", "Error during initialization: ${e.message}", e)
            }
        }
    }
}
