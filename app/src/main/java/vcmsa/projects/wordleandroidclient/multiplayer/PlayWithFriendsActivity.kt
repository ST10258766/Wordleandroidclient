package vcmsa.projects.wordleandroidclient.multiplayer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.wordleandroidclient.R

class PlayWithFriendsActivity : AppCompatActivity() {

    private lateinit var etRoomCode: EditText
    private lateinit var btnJoin: Button
    private lateinit var btnGenerate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_with_friends)
        supportActionBar?.title = getString(R.string.play_friends_title)

        etRoomCode = findViewById(R.id.etRoomCode)
        btnJoin = findViewById(R.id.btnJoin)
        btnGenerate = findViewById(R.id.btnGenerate)

        // Host: generate a new code and create a room
        btnGenerate.setOnClickListener {
            val code = generateRoomCode()
            lifecycleScope.launch {
                try {
                    MultiplayerRepository.createRoom(code)
                    Toast.makeText(
                        this@PlayWithFriendsActivity,
                        getString(R.string.toast_room_created, code),
                        Toast.LENGTH_SHORT
                    ).show()

                    goToWaitingRoom(code, isHost = true)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@PlayWithFriendsActivity,
                        getString(R.string.error_create_room_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Friend: join an existing room
        btnJoin.setOnClickListener {
            val rawCode = etRoomCode.text.toString().trim()

            if (rawCode.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.error_room_code_required),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val code = rawCode.uppercase()
            if (code.length != 6) {
                Toast.makeText(
                    this,
                    getString(R.string.error_room_code_invalid),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    MultiplayerRepository.joinRoom(code)
                    goToWaitingRoom(code, isHost = false)
                } catch (e: Exception) {
                    Toast.makeText(
                        this@PlayWithFriendsActivity,
                        e.message ?: getString(R.string.error_join_room_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun goToWaitingRoom(code: String, isHost: Boolean) {
        val intent = Intent(this, WaitingRoomActivity::class.java).apply {
            putExtra("roomCode", code)
            putExtra("isHost", isHost)
        }
        startActivity(intent)
    }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
