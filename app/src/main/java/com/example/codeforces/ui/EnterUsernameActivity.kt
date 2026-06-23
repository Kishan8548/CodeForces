package com.example.codeforces.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.databinding.ActivityEnterUsernameBinding
import com.example.codeforces.MainViewModel
import com.example.codeforces.widget.ProfileWidget
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EnterUsernameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnterUsernameBinding
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition { !viewModel.isReady.value }
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_X, 0.4f, 0.0f)
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 500L
                zoomX.doOnEnd { screen.remove() }

                val zoomY = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_Y, 0.4f, 0.0f)
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 500L
                zoomY.doOnEnd { screen.remove() }

                zoomX.start()
                zoomY.start()
            }
        }
        enableEdgeToEdge()

        // Dark status + nav bar
        window.statusBarColor = Color.parseColor("#0A0A0A")
        window.navigationBarColor = Color.parseColor("#0A0A0A")

        val prefs = getSharedPreferences("CodeforcesPrefs", MODE_PRIVATE)
        val savedHandle = prefs.getString("HANDLE", null)

        if (savedHandle != null) {
            ProfileWidget.triggerUpdate(this)
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("HANDLE", savedHandle)
            })
            finish()
            return
        }

        binding = ActivityEnterUsernameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etUsername.setOnEditorActionListener { _, actionId, event ->
            if ((event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) || actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnSubmit.performClick()
                true
            } else false
        }

        binding.btnSubmit.setOnClickListener {
            val handle = binding.etUsername.text.toString().trim()
            if (handle.isEmpty()) {
                Toast.makeText(this, "Please enter your Codeforces handle", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Loading state
            binding.btnSubmit.text = "VERIFYING..."
            binding.btnSubmit.isEnabled = false

            isValidUser(handle) { isValid ->
                binding.btnSubmit.text = "CONNECT →"
                binding.btnSubmit.isEnabled = true

                if (isValid) {
                    prefs.edit().putString("HANDLE", handle).apply()
                    ProfileWidget.triggerUpdate(this)
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        putExtra("HANDLE", handle)
                    })
                    finish()
                } else {
                    Toast.makeText(this, "Handle not found on Codeforces", Toast.LENGTH_SHORT).show()
                    // Shake animation on error
                    val shakeAnim = ObjectAnimator.ofFloat(
                        binding.etUsername, View.TRANSLATION_X,
                        0f, -16f, 16f, -12f, 12f, -8f, 8f, 0f
                    )
                    shakeAnim.duration = 400
                    shakeAnim.start()
                }
            }
        }
    }

    private fun isValidUser(handle: String, onResult: (Boolean) -> Unit) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getUserInfo(handle)
                val isValid = response.isSuccessful &&
                        response.body()?.status == "OK" &&
                        !response.body()?.result.isNullOrEmpty()
                onResult(isValid)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
