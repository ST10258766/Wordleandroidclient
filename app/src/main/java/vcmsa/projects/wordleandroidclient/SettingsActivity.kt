package vcmsa.projects.wordleandroidclient

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import vcmsa.projects.wordleandroidclient.data.SettingsStore
import vcmsa.projects.wordleandroidclient.LocaleManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var swDark: Switch
    private lateinit var swHaptics: Switch
    private lateinit var spLanguage: Spinner // Reference for the new language dropdown

    // CRITICAL: This is called before onCreate and ensures the correct language resources are loaded
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        // Use translated title
        supportActionBar?.title = getString(R.string.settings_title)

        spLanguage = findViewById(R.id.spLanguage)
        swDark = findViewById(R.id.swDarkTheme)
        swHaptics = findViewById(R.id.swHaptics)

        // --- Language Spinner Setup ---

        // Load the language options from the resource array
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.language_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLanguage.adapter = adapter

        // Set the initial selection based on the saved language code
        lifecycleScope.launch {
            SettingsStore.languageCodeFlow(this@SettingsActivity).collectLatest { code ->
                val index = if (code == "af") 1 else 0 // 1 for Afrikaans, 0 for English
                // SetSelection must be called carefully to avoid triggering the listener immediately
                if (spLanguage.selectedItemPosition != index) {
                    spLanguage.setSelection(index, false)
                }
            }
        }

        // Set up the change listener for the Spinner
        spLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Check if the selection actually changed
                if (id != -1L) { // id == -1L often indicates initial setup, which we handle above
                    lifecycleScope.launch {
                        // Save the new locale and apply it
                        LocaleManager.setNewLocale(this@SettingsActivity, position)
                        // Recreate the activity to force the UI to load the new strings immediately
                        recreate()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        // --- EXISTING THEME/HAPTICS LOGIC ---
        // (Existing code for collecting flows and handling switch changes remains the same)

        lifecycleScope.launch {
            SettingsStore.darkThemeFlow(this@SettingsActivity).collectLatest { v ->
                if (swDark.isChecked != v) swDark.isChecked = v
            }
        }
        lifecycleScope.launch {
            SettingsStore.hapticsFlow(this@SettingsActivity).collectLatest { v ->
                if (swHaptics.isChecked != v) swHaptics.isChecked = v
            }
        }

        swDark.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                SettingsStore.setDarkTheme(this@SettingsActivity, isChecked)
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                delegate.applyDayNight()
                recreate()
            }
        }

        swHaptics.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { SettingsStore.setHaptics(this@SettingsActivity, isChecked) }
        }
    }
}