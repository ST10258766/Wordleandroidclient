package vcmsa.projects.wordleandroidclient.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Fire the Daily WordRush notification
        NotificationHelper.showDailyWordRushReminder(context)
    }
}
