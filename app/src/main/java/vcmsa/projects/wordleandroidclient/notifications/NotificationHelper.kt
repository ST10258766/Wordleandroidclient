package vcmsa.projects.wordleandroidclient.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import vcmsa.projects.wordleandroidclient.DashboardActivity
import vcmsa.projects.wordleandroidclient.MainActivity
import vcmsa.projects.wordleandroidclient.R

object NotificationHelper {
    // Push notification logic adapted from:
// Brown, L. (2023). Firebase Push Notifications in Android. Available at: https://github.com/lbrown/firebase-push-notifications [Accessed 18 Nov. 2025]
    private const val CHANNEL_REMINDERS = "reminders_channel"
    private const val CHANNEL_BADGES = "badges_channel"

    const val ID_DAILY_WORDRUSH = 101
    const val ID_SPEEDLE = 102
    const val ID_BADGE_UNLOCKED = 103
    const val ID_TEST = 999

    // ---- SAFE NOTIFY ----
    private fun safeNotify(context: Context, id: Int, notif: Notification) {
        try {
            NotificationManagerCompat.from(context).notify(id, notif)
        } catch (_: SecurityException) {}
    }

    // ---- PERMISSION ----
    fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    // ---- CREATE CHANNELS ----
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val reminder = NotificationChannel(
                CHANNEL_REMINDERS,
                "WordRush Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )

            val badge = NotificationChannel(
                CHANNEL_BADGES,
                "Achievements & Badges",
                NotificationManager.IMPORTANCE_HIGH
            )

            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(reminder)
            nm.createNotificationChannel(badge)
        }
    }

    private fun dashboardIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, 0,
            Intent(context, DashboardActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun openDailyIntent(context: Context): PendingIntent {
        val i = Intent(context, MainActivity::class.java)
        i.putExtra("mode", "DAILY")
        return PendingIntent.getActivity(
            context,
            2001,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun openSpeedleIntent(context: Context): PendingIntent {
        val i = Intent(context, MainActivity::class.java)
        i.putExtra("mode", "SPEEDLE")
        i.putExtra("seconds", 90)
        return PendingIntent.getActivity(
            context,
            2002,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ---- DAILY ----
    @SuppressLint("MissingPermission")
    fun showDailyWordRushReminder(context: Context) {
        if (!hasPermission(context)) return

        val notif = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily WordRush is ready ✨")
            .setContentText("Can you keep your streak alive?")
            .setContentIntent(dashboardIntent(context))
            .addAction(0, "Play Now", openDailyIntent(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        safeNotify(context, ID_DAILY_WORDRUSH, notif)
    }

    // ---- SPEEDLE ----
    @SuppressLint("MissingPermission")
    fun showSpeedleReminder(context: Context) {
        if (!hasPermission(context)) return

        val notif = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Speedle Challenge ⏱️")
            .setContentText("Beat your fastest time!")
            .setContentIntent(dashboardIntent(context))
            .addAction(0, "Start Speedle", openSpeedleIntent(context))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        safeNotify(context, ID_SPEEDLE, notif)
    }

    // ---- BADGE UNLOCKED ----
    @SuppressLint("MissingPermission")
    fun showBadgeUnlocked(context: Context, badgeTitle: String, description: String) {
        if (!hasPermission(context)) return

        val notif = NotificationCompat.Builder(context, CHANNEL_BADGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Badge unlocked: $badgeTitle 🏅")
            .setContentText(description)
            .setContentIntent(dashboardIntent(context))
            .setAutoCancel(true)
            .build()

        safeNotify(context, ID_BADGE_UNLOCKED, notif)
    }

    // ---- TEST NOTIFICATION ----
    @SuppressLint("MissingPermission")
    fun showTestNotification(context: Context) {
        if (!hasPermission(context)) return

        val notif = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🔔 Test Notification")
            .setContentText("This is how your WordRush notifications look.")
            .setContentIntent(dashboardIntent(context))
            .setAutoCancel(true)
            .build()

        safeNotify(context, ID_TEST, notif)
    }
}
