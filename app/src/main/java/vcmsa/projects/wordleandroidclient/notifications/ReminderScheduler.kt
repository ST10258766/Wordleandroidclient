package vcmsa.projects.wordleandroidclient.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object ReminderScheduler {

    private const val REQ_DAILY_WORDRUSH = 2001
    private const val REQ_SPEEDLE = 2002

    fun scheduleAll(context: Context) {
        Log.d("REMINDER", "Scheduling all reminders…")
        scheduleDailyWordRush(context)
        scheduleSpeedle(context)
    }

    fun cancelAll(context: Context) {
        Log.d("REMINDER", "Cancelling all reminders…")
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val daily = PendingIntent.getBroadcast(
            context, REQ_DAILY_WORDRUSH,
            Intent(context, DailyWordRushReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        am.cancel(daily)

        val speedle = PendingIntent.getBroadcast(
            context, REQ_SPEEDLE,
            Intent(context, SpeedleReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        am.cancel(speedle)
    }

    private fun scheduleDailyWordRush(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, DailyWordRushReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, REQ_DAILY_WORDRUSH, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d("REMINDER", "Daily WordRush scheduled @ ${cal.time}")

        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pi
        )
    }

    private fun scheduleSpeedle(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, SpeedleReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, REQ_SPEEDLE, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d("REMINDER", "Speedle scheduled @ ${cal.time}")

        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pi
        )
    }
}
