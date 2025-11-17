package vcmsa.projects.wordleandroidclient

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import vcmsa.projects.wordleandroidclient.badges.BadgeManager
import vcmsa.projects.wordleandroidclient.badges.BadgesAdapter

class BadgesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)
        supportActionBar?.hide()

        val recycler = findViewById<RecyclerView>(R.id.recyclerBadges)
        recycler.layoutManager = GridLayoutManager(this, 2)

        lifecycleScope.launch {
            val states = BadgeManager.getBadgeStates()
            recycler.adapter = BadgesAdapter(states)
        }
    }
}
