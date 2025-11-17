package vcmsa.projects.wordleandroidclient

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import vcmsa.projects.wordleandroidclient.data.SettingsStore
import vcmsa.projects.wordleandroidclient.notifications.ReminderScheduler
import vcmsa.projects.wordleandroidclient.notifications.NotificationHelper
import vcmsa.projects.wordleandroidclient.badges.BadgeManager
import vcmsa.projects.wordleandroidclient.badges.BadgeConstants
import vcmsa.projects.wordleandroidclient.badges.BadgePopup

class SettingsActivity : AppCompatActivity() {

    private lateinit var swDark: Switch
    private lateinit var swHaptics: Switch
    private lateinit var swNotifications: Switch
    private lateinit var rowTestNotification: View
    private lateinit var rowTriggerNow: View

    companion object {
        private const val REQ_POST_NOTIFICATIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.title = "Settings"

        swDark = findViewById(R.id.swDarkTheme)
        swHaptics = findViewById(R.id.swHaptics)
        swNotifications = findViewById(R.id.swNotifications)
        rowTestNotification = findViewById(R.id.rowTestNotification)
        rowTriggerNow = findViewById(R.id.rowTriggerNow)

        // Observe states
        lifecycleScope.launch {
            SettingsStore.notificationsFlow(this@SettingsActivity).collectLatest { v ->
                if (swNotifications.isChecked != v) swNotifications.isChecked = v
            }
        }
       


        // Dark mode toggle
        swDark.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                SettingsStore.setDarkTheme(this@SettingsActivity, isChecked)
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                recreate()
            }
        }

        // Haptics toggle
        swHaptics.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { SettingsStore.setHaptics(this@SettingsActivity, isChecked) }
        }

        // Notifications toggle
        swNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (hasPermission()) enableNotifications()
                else requestPermission()
            } else {
                lifecycleScope.launch { SettingsStore.setNotifications(this@SettingsActivity, false) }
                ReminderScheduler.cancelAll(this)
                Toast.makeText(this, "Daily reminders turned off", Toast.LENGTH_SHORT).show()
            }
        }

        // Test notification
        rowTestNotification.setOnClickListener {
            NotificationHelper.showTestNotification(this)
        }

        // Hidden "Trigger now" for debugging
        rowTriggerNow.setOnClickListener {
            NotificationHelper.showDailyWordRushReminder(this)
            NotificationHelper.showSpeedleReminder(this)
            Toast.makeText(this, "Triggered both reminders", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableNotifications() {
        // Save preference
        lifecycleScope.launch {
            SettingsStore.setNotifications(this@SettingsActivity, true)
        }

        // Schedule both daily reminders
        ReminderScheduler.scheduleAll(this)

        // 🎖 NOTIFICATION GURU – badge for enabling notifications
        lifecycleScope.launch {
            val new = BadgeManager.unlock(BadgeConstants.NOTIFICATION_GURU)
            if (new) {
                val badge = BadgeConstants.getBadge(BadgeConstants.NOTIFICATION_GURU)!!
                BadgePopup.show(this@SettingsActivity, badge)
            }
        }

        Toast.makeText(
            this,
            "Daily WordRush & Speedle reminders scheduled",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQ_POST_NOTIFICATIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_POST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                enableNotifications()
            else
                swNotifications.isChecked = false
        }
    }
}
