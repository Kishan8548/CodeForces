package com.example.codeforces.ui

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.codeforces.databinding.ActivityMainBinding
import android.Manifest
import android.widget.Toast
import com.example.codeforces.R
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dark status bar
        window.statusBarColor = getColor(R.color.surface_black)
        window.navigationBarColor = getColor(R.color.surface_black)

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment

        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        // Deep-link from widget: select Profile tab
        if (intent?.getStringExtra("NAVIGATE_TO") == "profile") {
            binding.bottomNavigation.selectedItemId = R.id.profileFragment
        }

        askNotificationPermission()
    }

    /**
     * Called by ProfileFragment after rank is determined.
     * Animates the bottom nav active color + top border to the rank accent color.
     */
    fun applyRankTheme(accentColor: Int) {
        val states = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(accentColor, getColor(R.color.on_surface_variant))
        )
        binding.bottomNavigation.itemIconTintList = states
        binding.bottomNavigation.itemTextColor = states

        // Top border line flashes to rank color
        binding.navTopBorder.setBackgroundColor(accentColor)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 101)
            }
        }
    }
}