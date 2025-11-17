package vcmsa.projects.wordleandroidclient.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    // -------- Daily Lock + Game State Tracking --------
    private val LAST_PLAYED_DATE = stringPreferencesKey("last_played_date")
    private val LAST_PLAYED_USER = stringPreferencesKey("last_played_user")
    private val LAST_GUESSES = stringPreferencesKey("last_game_guesses")
    private val LAST_FEEDBACK_ROWS = stringPreferencesKey("last_game_feedback_rows")

    // --- FLOWS ---
    fun darkThemeFlow(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[DARK_THEME] ?: false }

    fun hapticsFlow(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[HAPTICS] ?: true }

    fun soundsFlow(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[SOUNDS] ?: true }

    fun notificationsFlow(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[NOTIFICATIONS] ?: false }

    // --- Setters ---
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

    // --- Today's Metadata ---
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

    // --- Per-User Daily Lock ---
    suspend fun setLastPlayedDate(ctx: Context, date: String, userId: String) {
        ctx.dataStore.edit {
            it[LAST_PLAYED_DATE] = date
            it[LAST_PLAYED_USER] = userId
        }
    }

    suspend fun hasUserPlayedToday(ctx: Context, date: String, userId: String): Boolean {
        val prefs = ctx.dataStore.data.first()
        return prefs[LAST_PLAYED_DATE] == date && prefs[LAST_PLAYED_USER] == userId
    }

    suspend fun getLastPlayedDate(ctx: Context): String? =
        ctx.dataStore.data.map { it[LAST_PLAYED_DATE] }.first()

    // --- Save Local Board ---
    suspend fun saveLastGameState(
        ctx: Context,
        guesses: List<String>,
        feedbackRows: List<List<String>>
    ) {
        val guessesStr = guesses.joinToString("|") { it.uppercase() }
        val feedbackStr = feedbackRows.joinToString("|") { row ->
            row.joinToString("") { it } // "GYAAY"
        }
        ctx.dataStore.edit {
            it[LAST_GUESSES] = guessesStr
            it[LAST_FEEDBACK_ROWS] = feedbackStr
        }
    }

    suspend fun getLastGameState(ctx: Context): Pair<List<String>, List<List<String>>>? {
        val prefs = ctx.dataStore.data.first()
        val g = prefs[LAST_GUESSES]
        val f = prefs[LAST_FEEDBACK_ROWS]

        if (g.isNullOrBlank() || f.isNullOrBlank()) return null

        val guesses = g.split("|").filter { it.isNotBlank() }
        val feedbackRows = f.split("|").map { row ->
            row.trim().map { it.toString() }
        }

        return guesses to feedbackRows
    }

    suspend fun clearLastGameState(ctx: Context) {
        ctx.dataStore.edit {
            it.remove(LAST_GUESSES)
            it.remove(LAST_FEEDBACK_ROWS)
        }
    }
}
