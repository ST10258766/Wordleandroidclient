package vcmsa.projects.wordleandroidclient.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("wordle_prefs")

object SettingsStore {

    // -------- Basic App Settings --------
    private val DARK_THEME = booleanPreferencesKey("dark_theme")
    private val HAPTICS = booleanPreferencesKey("haptics")
    private val SOUNDS = booleanPreferencesKey("sounds")
    private val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")

    private val TODAY_META_JSON = stringPreferencesKey("today_meta_json")
    private val TODAY_META_DATE = stringPreferencesKey("today_meta_date")

    // -------- Dynamic per-user keys --------
    private fun keyLastPlayed(uid: String) =
        stringPreferencesKey("last_played_date_$uid")

    private fun keyGameGuesses(uid: String) =
        stringPreferencesKey("last_guesses_$uid")

    private fun keyGameFeedback(uid: String) =
        stringPreferencesKey("last_feedback_$uid")

    // -------- Basic Settings --------
    fun darkThemeFlow(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[DARK_THEME] ?: false }

    fun hapticsFlow(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[HAPTICS] ?: true }

    fun soundsFlow(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[SOUNDS] ?: true }

    fun notificationsFlow(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[NOTIFICATIONS] ?: false }

    suspend fun setDarkTheme(ctx: Context, v: Boolean) {
        ctx.dataStore.edit { it[DARK_THEME] = v }
    }

    suspend fun setHaptics(ctx: Context, v: Boolean) {
        ctx.dataStore.edit { it[HAPTICS] = v }
    }

    suspend fun setSounds(ctx: Context, v: Boolean) {
        ctx.dataStore.edit { it[SOUNDS] = v }
    }

    suspend fun setNotifications(ctx: Context, v: Boolean) {
        ctx.dataStore.edit { it[NOTIFICATIONS] = v }
    }

    // -------- Today Metadata --------
    suspend fun saveTodayMetadata(ctx: Context, json: String, date: String) {
        ctx.dataStore.edit {
            it[TODAY_META_JSON] = json
            it[TODAY_META_DATE] = date
        }
    }

    suspend fun loadTodayMetadata(ctx: Context): Pair<String?, String?> {
        val prefs = ctx.dataStore.data.first()
        return prefs[TODAY_META_JSON] to prefs[TODAY_META_DATE]
    }

    // -------- PER USER: Last Played Date --------
    suspend fun setLastPlayedDate(ctx: Context, date: String, uid: String) {
        ctx.dataStore.edit {
            it[keyLastPlayed(uid)] = date
        }
    }

    suspend fun hasUserPlayedToday(ctx: Context, date: String, uid: String): Boolean {
        val prefs = ctx.dataStore.data.first()
        return prefs[keyLastPlayed(uid)] == date
    }

    suspend fun getLastPlayedDate(ctx: Context, uid: String): String? =
        ctx.dataStore.data.map { it[keyLastPlayed(uid)] }.first()

    // -------- PER USER: Save Local Game State --------
    suspend fun saveLastGameState(
        ctx: Context,
        guesses: List<String>,
        feedbackRows: List<List<String>>,
        uid: String
    ) {
        val gson = Gson()
        val guessJson = gson.toJson(guesses)
        val feedbackJson = gson.toJson(feedbackRows)

        ctx.dataStore.edit {
            it[keyGameGuesses(uid)] = guessJson
            it[keyGameFeedback(uid)] = feedbackJson
        }
    }

    suspend fun getLastGameState(ctx: Context, uid: String): Pair<List<String>, List<List<String>>>? {
        val prefs = ctx.dataStore.data.first()
        val g = prefs[keyGameGuesses(uid)]
        val f = prefs[keyGameFeedback(uid)]

        if (g.isNullOrBlank() || f.isNullOrBlank()) return null

        val gson = Gson()

        val guesses: List<String> =
            gson.fromJson(g, object : TypeToken<List<String>>() {}.type)

        val feedback: List<List<String>> =
            gson.fromJson(f, object : TypeToken<List<List<String>>>() {}.type)

        return guesses to feedback
    }

    suspend fun clearLastGameState(ctx: Context, uid: String) {
        ctx.dataStore.edit {
            it.remove(keyGameGuesses(uid))
            it.remove(keyGameFeedback(uid))
        }
    }
}
