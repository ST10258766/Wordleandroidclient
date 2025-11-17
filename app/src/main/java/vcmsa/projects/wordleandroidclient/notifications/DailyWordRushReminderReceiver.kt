package vcmsa.projects.wordleandroidclient.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyWordRushReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationHelper.showDailyWordRushReminder(context)
    }
}
