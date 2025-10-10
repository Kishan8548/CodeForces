package com.example.codeforces.ui


import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.codeforces.R
import com.example.codeforces.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val profileFragment = ProfileFragment()
        val contestFragment = ContestFragment()
        val problemsFragment = ProblemsFragment()


        setCurrentFragment(profileFragment)
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_profile -> setCurrentFragment(profileFragment)
                R.id.nav_contest -> setCurrentFragment(contestFragment)
                R.id.nav_problem -> setCurrentFragment(problemsFragment)
            }
            true
        }

    }

    private fun setCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit() 
        }
    }
}