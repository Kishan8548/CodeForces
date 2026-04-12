package com.example.codeforces.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.KeyEvent
import android.widget.TextView
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
import com.example.codeforces.models.ApiResponse
import com.example.codeforces.models.User
import com.example.codeforces.widget.ProfileWidget
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EnterUsernameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnterUsernameBinding
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_X,
                    0.4f,
                    0.0f
                )
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 500L
                zoomX.doOnEnd { screen.remove() }

                val zoomY = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_Y,
                    0.4f,
                    0.0f
                )
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 500L
                zoomY.doOnEnd { screen.remove() }

                zoomX.start()
                zoomY.start()
            }
        }
        enableEdgeToEdge()

        val prefs = getSharedPreferences("CodeforcesPrefs", MODE_PRIVATE)
        val savedHandle = prefs.getString("HANDLE", null)

        if (savedHandle != null) {
            // Refresh widget in case handle changed externally
            ProfileWidget.triggerUpdate(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("HANDLE", savedHandle)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityEnterUsernameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etUsername.setOnEditorActionListener { _, actionId, event ->
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)){
                binding.btnSubmit.performClick()
                true
            }else{
                false
            }
        }

        binding.btnSubmit.setOnClickListener {
            val handle = binding.etUsername.text.toString().trim()
            if (handle.isEmpty()) {
                Toast.makeText(this, "Please enter a handle", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isValidUser(handle) { isValid ->
                if (isValid) {
                    prefs.edit().putString("HANDLE", handle).apply()
                    // Refresh all widget instances with the new handle
                    ProfileWidget.triggerUpdate(this)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("HANDLE", handle)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid handle", Toast.LENGTH_SHORT).show()
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
