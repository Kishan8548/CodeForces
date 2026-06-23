package com.example.codeforces.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.databinding.ActivityHandleBindingBinding
import com.example.codeforces.models.User
import com.example.codeforces.repository.UserRepository
import com.example.codeforces.utils.RankUtils
import com.example.codeforces.utils.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HandleBindingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHandleBindingBinding
    private val auth = FirebaseAuth.getInstance()
    private var verifiedUser: User? = null
    private var isVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = Color.parseColor("#0A0A0A")
        window.navigationBarColor = Color.parseColor("#0A0A0A")

        binding = ActivityHandleBindingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Populate Google account info
        auth.currentUser?.let { user ->
            val firstName = user.displayName?.split(" ")?.firstOrNull() ?: "there"
            binding.tvGreeting.text = "Hey, $firstName!"
            binding.tvGoogleEmail.text = user.email ?: ""
            if (user.photoUrl != null) {
                Glide.with(this).load(user.photoUrl).circleCrop().into(binding.ivGoogleAvatar)
            }
        }

        binding.btnBack.setOnClickListener { finish() }

        // IME done → trigger verify
        binding.etHandle.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                binding.btnConfirmHandle.performClick()
                true
            } else false
        }

        binding.btnConfirmHandle.setOnClickListener {
            if (isVerified) {
                saveHandleAndProceed()
            } else {
                verifyHandle()
            }
        }
    }

    // ─── Step 1: Verify the handle against CF API ─────────────────────────────

    private fun verifyHandle() {
        val handle = binding.etHandle.text.toString().trim()
        if (handle.isBlank()) {
            Toast.makeText(this, "Enter your Codeforces handle", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        binding.btnConfirmHandle.text = "VERIFYING..."

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getUserInfo(handle)
                if (response.isSuccessful && response.body()?.status == "OK") {
                    val user = response.body()?.result?.firstOrNull()
                    if (user != null) {
                        verifiedUser = user
                        showPreviewCard(user)
                    } else {
                        handleError("Handle not found")
                    }
                } else {
                    handleError("Handle not found on Codeforces")
                }
            } catch (e: Exception) {
                handleError("Network error: ${e.message}")
            }
        }
    }

    // ─── Step 2: Show preview card with rank + rating ─────────────────────────

    private fun showPreviewCard(user: User) {
        setLoading(false)
        isVerified = true

        val (rankLabel, _) = RankUtils.applyTheme(this, user.rating)
        val theme = ThemeManager.current

        binding.previewCard.visibility = View.VISIBLE
        binding.tvPreviewHandle.text = user.handle
        binding.tvPreviewHandle.setTextColor(theme.primary)
        binding.tvPreviewRank.text = rankLabel.uppercase()
        binding.tvPreviewRank.backgroundTintList = ColorStateList.valueOf(theme.primary)
        binding.tvPreviewRank.setTextColor(theme.onPrimary)
        binding.tvPreviewRating.text = "Rating: ${user.rating ?: "Unrated"}"
        binding.tvPreviewCheck.imageTintList = ColorStateList.valueOf(theme.primary)

        // Update button to confirm
        binding.btnConfirmHandle.text = "CONFIRM & CONTINUE →"
        binding.btnConfirmHandle.backgroundTintList = ColorStateList.valueOf(theme.primary)
        binding.btnConfirmHandle.setTextColor(theme.onPrimary)
        binding.btnConfirmShadow.setBackgroundColor(theme.primaryDim)

        // Animate preview card in
        binding.previewCard.alpha = 0f
        binding.previewCard.animate().alpha(1f).setDuration(300).start()
    }

    // ─── Step 3: Save to SharedPrefs + Firestore → go to main ────────────────

    private fun saveHandleAndProceed() {
        val user = verifiedUser ?: return
        setLoading(true)
        binding.btnConfirmHandle.text = "SAVING..."

        lifecycleScope.launch {
            // Save locally
            val prefs = getSharedPreferences("CodeforcesPrefs", MODE_PRIVATE)
            prefs.edit().putString("HANDLE", user.handle).apply()

            // Save to Firestore if signed in
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                UserRepository.saveUser(firebaseUser, user.handle)
            }

            // Trigger widget update
            com.example.codeforces.widget.ProfileWidget.triggerUpdate(this@HandleBindingActivity)

            // Navigate to main
            startActivity(Intent(this@HandleBindingActivity, MainActivity::class.java).also {
                it.putExtra("HANDLE", user.handle)
            })
            finish()
        }
    }

    private fun handleError(msg: String) {
        setLoading(false)
        isVerified = false
        binding.btnConfirmHandle.text = "VERIFY HANDLE"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        // Shake the input
        val shake = ObjectAnimator.ofFloat(binding.etHandle, View.TRANSLATION_X,
            0f, -16f, 16f, -12f, 12f, -8f, 8f, 0f)
        shake.duration = 400
        shake.start()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBinding.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnConfirmHandle.isEnabled = !loading
        binding.etHandle.isEnabled = !loading
    }
}
