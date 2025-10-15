package com.example.codeforces.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.databinding.ActivityEnterUsernameBinding
import com.example.codeforces.models.ApiResponse
import com.example.codeforces.models.User

class EnterUsernameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnterUsernameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
