package vcmsa.projects.wordleandroidclient.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.wordleandroidclient.R

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.VH>() {

    private val items = mutableListOf<LeaderboardEntry>()

    fun submit(list: List<LeaderboardEntry>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val item = items[pos]

        // Rank with medal emojis for top 3
        holder.tvRank.text = when (pos) {
            0 -> "🥇"
            1 -> "🥈"
            2 -> "🥉"
            else -> "#${pos + 1}"
        }

        // Username
        holder.tvName.text = item.username ?: "Player"

        // Subtitle: show guesses used and time left
        val guessText = if (item.guessesUsed == 1) "1 guess" else "${item.guessesUsed} guesses"
        holder.tvSub.text = "$guessText • ${item.timeRemainingSec}s left"

        // Score (formatted with commas for readability)
        holder.tvScore.text = String.format("%,d", item.score)

        // Avatar
        holder.imgAvatar.setImageResource(R.drawable.wordrush_logo)
    }

    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvRank: TextView = v.findViewById(R.id.tvRank)
        val imgAvatar: ImageView = v.findViewById(R.id.imgAvatar)
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvSub: TextView = v.findViewById(R.id.tvSub)
        val tvScore: TextView = v.findViewById(R.id.tvScore)
    }
}