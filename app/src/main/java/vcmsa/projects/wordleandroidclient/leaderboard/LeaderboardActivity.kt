package vcmsa.projects.wordleandroidclient.leaderboard

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import vcmsa.projects.wordleandroidclient.R

class LeaderboardActivity : AppCompatActivity() {

    private val repo by lazy { ResultsLeaderboardRepo(FirebaseFirestore.getInstance()) }
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvLeaderboard)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        adapter = LeaderboardAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // Get duration from intent or default to 90
        val duration = intent.getIntExtra("duration", 90)

        lifecycleScope.launch {
            try {
                Log.d("Leaderboard", "Starting fetch in Activity...")

                val entries = repo.fetchSpeedleLeaderboard(
                    durationSec = duration,
                    limit = 50
                )

                Log.d("Leaderboard", "Received ${entries.size} entries in Activity")

                if (entries.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                    tvEmpty.text = "No results yet — be the first to win Speedle today!"
                } else {
                    tvEmpty.visibility = View.GONE
                    rv.visibility = View.VISIBLE
                    adapter.submit(entries)
                }
            } catch (e: Exception) {
                Log.e("Leaderboard", "Error in Activity", e)
                tvEmpty.visibility = View.VISIBLE
                rv.visibility = View.GONE
                tvEmpty.text = "Error loading leaderboard: ${e.message}"
            }
        }
    }
}