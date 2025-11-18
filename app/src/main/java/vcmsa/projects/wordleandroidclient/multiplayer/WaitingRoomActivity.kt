package vcmsa.projects.wordleandroidclient.multiplayer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vcmsa.projects.wordleandroidclient.MainActivity
import vcmsa.projects.wordleandroidclient.R

class WaitingRoomActivity : AppCompatActivity() {

    private lateinit var tvRoomCode: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnDevStart: Button

    private var listener: ListenerRegistration? = null
    private var roomCode: String = ""
    private var isHost: Boolean = false
    private var gameStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting_room)
        supportActionBar?.title = getString(R.string.room_title)

        tvRoomCode = findViewById(R.id.tvRoomCode)
        tvStatus = findViewById(R.id.tvStatus)
        btnCancel = findViewById(R.id.btnCancel)
        btnDevStart = findViewById(R.id.btnFakeStart)

        roomCode = intent.getStringExtra("roomCode") ?: ""
        isHost = intent.getBooleanExtra("isHost", false)

        tvRoomCode.text = roomCode
        tvStatus.text = getString(R.string.room_status_waiting)

        // Listen for second player joining
        listener = MultiplayerRepository.listenForPlayerCount(roomCode) { count ->
            if (count >= 2 && !gameStarted) {
                tvStatus.text = getString(R.string.room_status_ready)
                startGame()
            }
        }

        btnCancel.setOnClickListener {
            lifecycleScope.launch {
                if (isHost) {
                    // Host can delete the room
                    MultiplayerRepository.cancelRoom(roomCode)
                }
                finish()
            }
        }

        // Dev / fallback button: force start if needed
        btnDevStart.setOnClickListener {
            if (!gameStarted) {
                startGame()
            }
        }
    }

    private fun startGame() {
        if (gameStarted) return
        gameStarted = true

        listener?.remove()
        listener = null

        Toast.makeText(
            this,
            getString(R.string.room_starting),
            Toast.LENGTH_SHORT
        ).show()

        // 🔥 Decide and share the target word via Firestore
        lifecycleScope.launch {
            val db = FirebaseFirestore.getInstance()
            val roomRef = db.collection("rooms").document(roomCode)

            val targetWord: String = try {
                if (isHost) {
                    // Host chooses a word (from your local list) and saves it
                    val word = pickTargetWordFromAssetsOrFallback()
                    roomRef.update("targetWord", word).await()
                    word
                } else {
                    // Friend waits for host’s targetWord
                    val snap = roomRef.get().await()
                    snap.getString("targetWord") ?: "CRANE"
                }
            } catch (e: Exception) {
                // Ultimate fallback
                "CRANE"
            }

            val intent = Intent(this@WaitingRoomActivity, MainActivity::class.java).apply {
                putExtra("mode", "FRIENDS_MULTIPLAYER")
                putExtra("roomCode", roomCode)
                putExtra("targetWord", targetWord)
            }
            startActivity(intent)
            finish()
        }
    }

    /**
     * Uses the same 5-letter word list as your game (assets/wordlist_en_5.txt).
     * If it fails for some reason, returns "CRANE" as a fallback.
     */
    private fun pickTargetWordFromAssetsOrFallback(): String {
        return try {
            val words = assets.open("wordlist_en_5.txt")
                .bufferedReader()
                .readLines()
                .map { it.trim().uppercase() }
                .filter { it.length == 5 }

            if (words.isNotEmpty()) words.random() else "CRANE"
        } catch (e: Exception) {
            "CRANE"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
        listener = null
    }
}
