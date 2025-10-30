package com.example.codeforces.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
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
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("HANDLE", savedHandle)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityEnterUsernameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            val handle = binding.etUsername.text.toString().trim()
            if (handle.isEmpty()) {
                Toast.makeText(this, "Please enter a handle", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            isValidUser(handle) { isValid ->
                if (isValid) {
                    prefs.edit().putString("HANDLE", handle).apply()
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
        RetrofitInstance.api.getUserInfo(handle)
            .enqueue(object : retrofit2.Callback<ApiResponse<List<User>>> {
                override fun onResponse(
                    call: retrofit2.Call<ApiResponse<List<User>>>,
                    response: retrofit2.Response<ApiResponse<List<User>>>
                ) {
                    val body = response.body()
                    val isValid = response.isSuccessful && body?.status == "OK" && !body.result.isNullOrEmpty()
                    onResult(isValid)
                }

                override fun onFailure(
                    call: retrofit2.Call<ApiResponse<List<User>>>,
                    t: Throwable
                ) {
                    onResult(false)
                }
            })
    }
}
