package vcmsa.projects.wordleandroidclient

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vcmsa.projects.wordleandroidclient.UserProfile

class RegisterActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private lateinit var etFullName: EditText

    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirm: EditText
    private lateinit var btnSignup: Button
    private lateinit var tvLoginLink: TextView
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        etFullName = findViewById(R.id.etFullName)
        etEmail    = findViewById(R.id.etEmail)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirm  = findViewById(R.id.etConfirm)
        btnSignup  = findViewById(R.id.btnSignup)
        tvLoginLink= findViewById(R.id.tvLoginLink)
        progress   = findViewById(R.id.progress)

        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnSignup.setOnClickListener { attemptSignup() }
    }

    private fun attemptSignup() {
        val full = etFullName.text.toString().trim()
        val email= etEmail.text.toString().trim()
        val user = etUsername.text.toString().trim()
        val pass = etPassword.text.toString()
        val confirm = etConfirm.text.toString()


        // Field validation
        if (full.isEmpty()) {
            toast(getString(R.string.msg_signup_fullname_required)); return
        }
        if (!isValidEmail(email)) {
            toast(getString(R.string.msg_signup_invalid_email)); return
        }
        if (user.length < 3) {
            toast(getString(R.string.msg_signup_username_short)); return
        }
        if (!isStrongPassword(pass)) {
            toast(getString(R.string.msg_signup_password_weak)); return
        }
        if (pass != confirm) {
            toast(getString(R.string.msg_signup_password_mismatch)); return
        }

        lifecycleScope.launch {
            setBusy(true)
            try {
                // 1) case-insensitive username availability
                val taken = db.collection("profiles")
                    .whereEqualTo("usernameLower", user.lowercase())
                    .limit(1).get().await().isEmpty.not()
                if (taken) {
                    toast(getString(R.string.msg_signup_username_taken)); return@launch

                }

                // 2) create auth user
                auth.createUserWithEmailAndPassword(email, pass).await()
                val u = auth.currentUser ?: error("No user")

                // 3) create profile (also write usernameLower)
                val profile = UserProfile(
                    uid = u.uid,
                    fullName = full,
                    email = email,
                    username = user,
                    photoUrl = u.photoUrl?.toString()
                )
                val doc = db.collection("profiles").document(u.uid)
                doc.set(profile).await()
                doc.update("usernameLower", user.lowercase()).await()

                // 4) go to dashboard
                startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                finish()
            } catch (e: Exception) {
                toast(friendlyAuthError(e))
            } finally {
                setBusy(false)
            }
        }
    }

    // ---------- helpers ----------
    private fun isValidEmail(s: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()

    private fun isStrongPassword(p: String): Boolean = p.length >= 6

    private fun setBusy(b: Boolean) {
        progress.visibility = if (b) View.VISIBLE else View.GONE
        btnSignup.isEnabled = !b
        etFullName.isEnabled = !b
        etEmail.isEnabled = !b
        etUsername.isEnabled = !b
        etPassword.isEnabled = !b
        etConfirm.isEnabled = !b
    }

    private fun friendlyAuthError(e: Exception): String {
        val code = (e as? com.google.firebase.auth.FirebaseAuthException)?.errorCode ?: ""
        return when (code) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "That email is already in use."
            "ERROR_INVALID_EMAIL"        -> "Invalid email address."
            "ERROR_WEAK_PASSWORD"        -> "Choose a stronger password."
            else -> e.localizedMessage ?: "Sign up failed"
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
