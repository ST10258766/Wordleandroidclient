package vcmsa.projects.wordleandroidclient

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class GameBoardAdapter(
    private var letters: List<String>,
    private var states: List<TileState>
) : RecyclerView.Adapter<GameBoardAdapter.LetterBlockViewHolder>() {

    /** Number of columns in a row (used for stagger timing). Defaults to 5. */
    var wordLength: Int = 5

    /** Remember which positions we’ve already animated (prevents repeat flips). */
    private val animatedPositions = mutableSetOf<Int>()

    inner class LetterBlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val letterTextView: TextView = itemView.findViewById(R.id.tvLetterBlock)
        private val blockCard: MaterialCardView = itemView.findViewById(R.id.cardBlock)

        fun bindAt(position: Int, letter: String, state: TileState) {
            letterTextView.text = letter

            val isFinal = state == TileState.CORRECT ||
                    state == TileState.PRESENT ||
                    state == TileState.ABSENT

            val bgColor: Int
            val fgColor: Int
            val strokeColor: Int
            val strokeWidth: Int

            when (state) {
                TileState.CORRECT -> {
                    bgColor = Color.parseColor("#22C55E")       // neon green
                    fgColor = Color.WHITE
                    strokeColor = Color.parseColor("#4ADE80")
                    strokeWidth = 3
                }
                TileState.PRESENT -> {
                    bgColor = Color.parseColor("#EAB308")       // gold
                    fgColor = Color.WHITE
                    strokeColor = Color.parseColor("#FACC15")
                    strokeWidth = 3
                }
                TileState.ABSENT -> {
                    bgColor = Color.parseColor("#1E293B")       // dark slate
                    fgColor = Color.parseColor("#9CA3AF")
                    strokeColor = Color.TRANSPARENT
                    strokeWidth = 0
                }
                TileState.FILLED -> {
                    // active row typing – dark tile with neon outline
                    bgColor = Color.parseColor("#020617")
                    fgColor = Color.WHITE
                    strokeColor = Color.parseColor("#38BDF8")   // neon blue
                    strokeWidth = 3
                }
                TileState.EMPTY -> {
                    bgColor = Color.parseColor("#020617")
                    fgColor = Color.parseColor("#64748B")
                    strokeColor = Color.parseColor("#1E293B")
                    strokeWidth = 2
                }
            }

            blockCard.setCardBackgroundColor(bgColor)
            blockCard.strokeColor = strokeColor
            blockCard.strokeWidth = strokeWidth
            letterTextView.setTextColor(fgColor)

            // Flip animation only for final states, and only once per position
            if (isFinal && animatedPositions.add(position)) {
                val colIndex = if (wordLength > 0) position % wordLength else 0
                val delay = 70L * colIndex
                flipReveal(blockCard, letterTextView, bgColor, fgColor, delay)
            }
        }


        private fun flipReveal(
            card: CardView,
            tv: TextView,
            bgColor: Int,
            fgColor: Int,
            delay: Long
        ) {
            // Simple two-step "flip": scaleY down, swap color, scaleY up
            card.animate()
                .setStartDelay(delay)
                .scaleY(0f)
                .setDuration(110L)
                .withEndAction {
                    card.setCardBackgroundColor(bgColor)
                    tv.setTextColor(fgColor)
                    card.animate()
                        .scaleY(1f)
                        .setDuration(110L)
                        .start()
                }
                .start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterBlockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_letter_block, parent, false)
        return LetterBlockViewHolder(view)
    }

    override fun onBindViewHolder(holder: LetterBlockViewHolder, position: Int) {
        holder.bindAt(position, letters[position], states[position])
    }

    override fun getItemCount(): Int = letters.size

    /** Update just the letters . */
    fun updateLetters(newLetters: List<String>) {
        val sizeChanged = newLetters.size != letters.size
        letters = newLetters
        if (sizeChanged) animatedPositions.clear() // board reset
        notifyDataSetChanged()
    }

    /** Update states without forcing animations (used for non-reveal changes). */
    fun updateStates(newStates: List<TileState>) {
        val sizeChanged = newStates.size != states.size
        states = newStates
        if (sizeChanged) animatedPositions.clear() // board reset
        notifyDataSetChanged()
    }


    fun revealRow(startIndex: Int, len: Int = wordLength) {
        // Allow this row to animate even if some items were previously bound
        for (i in 0 until len) animatedPositions.remove(startIndex + i)
        notifyItemRangeChanged(startIndex, len)
    }
}
