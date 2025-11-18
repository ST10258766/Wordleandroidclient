package vcmsa.projects.wordleandroidclient

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vcmsa.projects.wordleandroidclient.data.SettingsStore
import vcmsa.projects.wordleandroidclient.leaderboard.LeaderboardActivity
import vcmsa.projects.wordleandroidclient.multiplayer.MultiplayerModeActivity
import vcmsa.projects.wordleandroidclient.multiplayer.PlayWithAIActivity
import vcmsa.projects.wordleandroidclient.multiplayer.PlayWithFriendsActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    private lateinit var tvGreeting: TextView
    private lateinit var chipStreak: TextView
    private lateinit var tvDailyCountdown: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnAI: Button
    private lateinit var cardDaily: View

    private var countdown: CountDownTimer? = null
    private var dailyPulse: AnimatorSet? = null // kept for safety, but we now use XML pulse anim

    override fun onStart() {
        super.onStart()
        Log.e("DASHBOARD", "===== onStart called =====")
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        refreshDailyCardState()
    }

    override fun onResume() {
        super.onResume()
        Log.e("DASHBOARD", "===== onResume called =====")
        refreshDailyCardState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        supportActionBar?.hide()

        tvGreeting       = findViewById(R.id.tvGreeting)
        chipStreak       = findViewById(R.id.chipStreak)
        tvDailyCountdown = findViewById(R.id.tvDailyCountdown)
        bottomNav        = findViewById(R.id.bottomNav)
        cardDaily        = findViewById(R.id.cardDaily)
        btnAI            = findViewById(R.id.btnAI)

        val quickActionsRow  = findViewById<View>(R.id.quickActionsRow)
        val cardSpeedle      = findViewById<View>(R.id.cardSpeedle)
        val cardPlayAi       = findViewById<View>(R.id.cardPlayAi)
        val cardLeaderboard  = findViewById<View>(R.id.cardLeaderboard)
        val cardBadges       = findViewById<View>(R.id.cardBadges)
        val btnHowToTop      = findViewById<View>(R.id.btnHowToTop)
        val btnSpeedleStart  = findViewById<Button>(R.id.btnSpeedleStart)

        // Show bottom-nav icons in their own neon colours
        bottomNav.itemIconTintList = null

        // Greeting text + time-of-day flavour
        val user = auth.currentUser
        val baseName = user?.displayName ?: user?.email?.substringBefore("@") ?: "Player"
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val prefix = when (hour) {
            in 5..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else      -> "Hey night owl"
        }
        tvGreeting.text = "$prefix, $baseName"

        // Streak chip – uses StatsManager
        val stats = StatsManager.getStats(this)
        chipStreak.text = if (stats.currentStreak > 0) {
            "🔥 ${stats.currentStreak}-day streak"
        } else {
            "Start a streak today"
        }

        // --- QUICK ACTIONS ---

        // Daily tile – same behaviour as main Daily card
        findViewById<View>(R.id.qaDaily).setOnClickListener {
            cardDaily.performClick()
        }

        // Speedle quick action – open duration chooser
        findViewById<View>(R.id.qaSpeedle).setOnClickListener {
            showSpeedleDurationChooser()
        }

        // Play vs AI quick action
        findViewById<View>(R.id.qaPlayAi).setOnClickListener {
            startActivity(Intent(this, PlayWithAIActivity::class.java))
        }

        // Friends quick action
        findViewById<View>(R.id.qaFriends).setOnClickListener {
            startActivity(Intent(this, PlayWithFriendsActivity::class.java))
        }

        // Leaderboard quick action
        findViewById<View>(R.id.qaLeaderboard).setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        // Badges quick action
        findViewById<View>(R.id.qaBadges).setOnClickListener {
            startActivity(Intent(this, BadgesActivity::class.java))
        }

        // Stats quick action – opens StatsActivity
        findViewById<View>(R.id.qaStats).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        // Top-right help icon
        btnHowToTop.setOnClickListener { showHowToDialog() }

        // AI big card button
        btnAI.setOnClickListener {
            startActivity(Intent(this, PlayWithAIActivity::class.java))
        }

        // Speedle radio group default
        val rg = findViewById<RadioGroup>(R.id.rgSpeedle).apply {
            check(R.id.rb90)
        }

        // Speedle "Start" button – user chooses duration then taps this
        btnSpeedleStart.setOnClickListener {
            val seconds = when (rg.checkedRadioButtonId) {
                R.id.rb60  -> 60
                R.id.rb120 -> 120
                else       -> 90
            }
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    putExtra("mode", "SPEEDLE")
                    putExtra("seconds", seconds)
                }
            )
        }

        // Mini cards
        cardLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }
        cardBadges.setOnClickListener {
            startActivity(Intent(this, BadgesActivity::class.java))
        }

        // Bottom nav behaviour
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_speedle -> {
                    showSpeedleDurationChooser()
                    true
                }

                R.id.nav_multiplayer -> {
                    startActivity(Intent(this, MultiplayerModeActivity::class.java))
                    true
                }

                R.id.nav_leaderboard -> {
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_home

        startResetCountdown()
        refreshDailyCardState()

        //  Simple fade-up animations when screen opens
        tvGreeting.fadeInUp(0)
        chipStreak.fadeInUp(40)
        quickActionsRow.fadeInUp(80)
        cardDaily.fadeInUp(160)
        cardSpeedle.fadeInUp(240)
        cardPlayAi.fadeInUp(320)
        cardLeaderboard.fadeInUp(400)
        cardBadges.fadeInUp(480)
    }

    // --- Small helpers for animations ---

    private fun View.fadeInUp(delay: Long = 0L, duration: Long = 400L) {
        alpha = 0f
        translationY = 40f
        animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(duration)
            .start()
    }

    private fun View.startPulse() {
        val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, 1.04f)
        val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, 1.04f)
        scaleX.repeatMode = ValueAnimator.REVERSE
        scaleY.repeatMode = ValueAnimator.REVERSE
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatCount = ValueAnimator.INFINITE
        scaleX.duration = 900
        scaleY.duration = 900

        dailyPulse = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            start()
        }
    }

    private fun stopDailyPulse() {
        dailyPulse?.cancel()
        dailyPulse = null
        cardDaily.scaleX = 1f
        cardDaily.scaleY = 1f
    }

    // --- Dialog helpers ---

    private fun showSpeedleDurationChooser() {
        val durations = arrayOf("60 seconds", "90 seconds", "120 seconds")
        val options = intArrayOf(60, 90, 120)
        AlertDialog.Builder(this)
            .setTitle("Play Speedle")
            .setItems(durations) { _, which ->
                startActivity(
                    Intent(this, MainActivity::class.java).apply {
                        putExtra("mode", "SPEEDLE")
                        putExtra("seconds", options[which])
                    }
                )
            }
            .show()
    }

    private fun showHowToDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_how_to_play, null)
        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Got it") { d, _ -> d.dismiss() }
            .show()
    }

    // --- Daily reset countdown ---

    private fun startResetCountdown() {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val target = cal.timeInMillis
        val duration = (target - System.currentTimeMillis()).coerceAtLeast(0)

        countdown?.cancel()
        countdown = object : CountDownTimer(duration, 1000) {
            override fun onTick(ms: Long) {
                val h = TimeUnit.MILLISECONDS.toHours(ms)
                val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
                val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
                tvDailyCountdown.text = String.format("Resets in %02d:%02d:%02d", h, m, s)
            }

            override fun onFinish() {
                tvDailyCountdown.text = "New puzzle ready!"
            }
        }.start()
    }

    // --- Daily card played/not played logic ---

    private fun refreshDailyCardState() {
        Log.e("DASHBOARD", "refreshDailyCardState started")

        lifecycleScope.launch {
            var played = false
            val today = getTodayIso()

            try {
                val user = auth.currentUser
                if (user == null) {
                    Log.e("DASHBOARD", "ERROR: no logged-in user")
                    applyDailyCardState(false)
                    return@launch
                }

                val uid = user.uid

                val localPlayed = SettingsStore.hasUserPlayedToday(
                    this@DashboardActivity,
                    today,
                    uid
                )

                played = if (localPlayed) {
                    true
                } else {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val docId = "${today}_en-ZA_${uid}"

                    try {
                        val doc = db.collection("results").document(docId).get().await()
                        val firestorePlayed = doc.exists()
                        if (firestorePlayed) {
                            SettingsStore.setLastPlayedDate(
                                this@DashboardActivity,
                                today,
                                uid
                            )
                            true
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        Log.e("DASHBOARD", "Firestore error: ${e.message}")
                        localPlayed
                    }
                }

            } catch (e: Exception) {
                Log.e("DASHBOARD", "Error: ${e.message}", e)
                played = false
            }

            applyDailyCardState(played)
        }
    }

    private fun applyDailyCardState(played: Boolean) {
        Log.e("DASHBOARD", "applyDailyCardState: played=$played")

        if (played) {
            cardDaily.clearAnimation()
            cardDaily.alpha = 0.6f
            cardDaily.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Daily played")
                    .setMessage("You've already played today. Come back tomorrow!")
                    .setPositiveButton("OK", null)
                    .show()
            }
        } else {
            cardDaily.alpha = 1f
            cardDaily.clearAnimation()
            // soft pulse XML animation
            cardDaily.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.pulse_soft)
            )
            cardDaily.setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    override fun onDestroy() {
        countdown?.cancel()
        stopDailyPulse()
        super.onDestroy()
    }

    private fun getTodayIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }
}
