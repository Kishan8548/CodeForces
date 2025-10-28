package com.example.codeforces.ui

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.codeforces.R
import com.example.codeforces.databinding.ActivityMainBinding
import android.Manifest
import android.util.Log
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val NOTIFICATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val handle = intent.getStringExtra("HANDLE")

        val profileFragment = ProfileFragment().apply {
            arguments = Bundle().apply {
                putString("HANDLE", handle)
            }
        }
        val contestFragment = ContestFragment()
        val problemsFragment = ProblemsFragment()

        setCurrentFragment(profileFragment)

        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_profile -> setCurrentFragment(profileFragment)
                R.id.nav_contest -> setCurrentFragment(contestFragment)
                R.id.nav_problem -> setCurrentFragment(problemsFragment)
            }
            true
        }
        Log.d("PERMISSION", "Checking notification permission...")
        askNotificationPermission()
    }
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 101)
            } else {

            }
        } else {
            Toast.makeText(this, "Below Android 13 — no permission needed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
    }
}
