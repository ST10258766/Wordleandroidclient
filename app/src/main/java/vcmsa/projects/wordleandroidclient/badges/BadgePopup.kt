package vcmsa.projects.wordleandroidclient.badges

import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import vcmsa.projects.wordleandroidclient.R

object BadgePopup {

    fun show(context: Context, badge: Badge) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.badge_popup, null)

        val img = view.findViewById<android.widget.ImageView>(R.id.imgBadge)
        val title = view.findViewById<android.widget.TextView>(R.id.tvTitle)
        val desc = view.findViewById<android.widget.TextView>(R.id.tvDescription)
        val ok = view.findViewById<android.widget.Button>(R.id.btnOk)

        img.setImageResource(badge.iconUnlocked)
        title.text = badge.title
        desc.text = badge.description

        // Play sound
        MediaPlayer.create(context, R.raw.badge_unlock_sound)?.start()


        ok.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.show()
    }
}
