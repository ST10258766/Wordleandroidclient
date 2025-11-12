package vcmsa.projects.wordleandroidclient


import android.content.Context
import android.os.Build
import vcmsa.projects.wordleandroidclient.data.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

/**
 * Manages the application's locale for multi-language support.
 * It changes the system's Configuration to load strings from the correct resources folder.
 */
object LocaleManager {

    private const val LANGUAGE_ENGLISH = "en"
    private const val LANGUAGE_AFRIKAANS = "af"

    /**
     * Retrieves the current saved language code from DataStore (synchronously).
     */
    private fun getLanguageCode(context: Context): String {
        return runBlocking { SettingsStore.languageCodeFlow(context).first() }
    }

    /**
     * Called when the user selects a new language in Settings.
     * Saves the preference and updates the resources.
     */
    fun setNewLocale(context: Context, languageIndex: Int): Context {
        val newLanguageCode = when (languageIndex) {
            1 -> LANGUAGE_AFRIKAANS // Index 1 is Afrikaans
            else -> LANGUAGE_ENGLISH // Index 0 or anything else is English
        }

        // Save the new code
        runBlocking { SettingsStore.setLanguageCode(context, newLanguageCode) }

        return updateResources(context, newLanguageCode)
    }

    /**
     * Core logic to update the base context's configuration.
     */
    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration

        // Handle API differences for setting locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        // Return a new context with the updated configuration
        return context.createConfigurationContext(configuration)
    }

    /**
     * Attaches the new context configuration to the base context of the activity.
     * This MUST be called in every Activity's attachBaseContext().
     */
    fun wrap(context: Context): Context {
        val languageCode = getLanguageCode(context)
        return updateResources(context, languageCode)
    }
}