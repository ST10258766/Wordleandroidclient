package vcmsa.projects.wordleandroidclient.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SpeedleReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationHelper.showSpeedleReminder(context)
    }
}
