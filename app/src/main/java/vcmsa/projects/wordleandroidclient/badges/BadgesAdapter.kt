package vcmsa.projects.wordleandroidclient.badges

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.wordleandroidclient.R

class BadgesAdapter(private val items: List<BadgeDisplay>) :
    RecyclerView.Adapter<BadgesAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(h: Holder, pos: Int) {
        val d = items[pos]

        // Set icon (locked or unlocked)
        h.icon.setImageResource(d.displayIcon)

        // Badge title
        h.title.text = d.badge.title

        // Subtitle:
        // If unlocked → show "Unlocked"
        // If locked → show requirement / description
        h.sub.text = if (d.unlocked) "Unlocked" else d.badge.description
    }

    override fun getItemCount() = items.size

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.imgBadge)
        val title: TextView = v.findViewById(R.id.tvTitle)
        val sub: TextView = v.findViewById(R.id.tvSubtitle)
    }
}
