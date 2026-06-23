package com.example.codeforces.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.codeforces.R
import com.example.codeforces.adapter.SubmissionsAdapter
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.databinding.ActivityFriendProfileBinding
import com.example.codeforces.repository.UserRepository
import com.example.codeforces.utils.RankUtils
import com.example.codeforces.utils.ThemeManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.random.Random

class FriendProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendProfileBinding
    private lateinit var submissionsAdapter: SubmissionsAdapter
    private val auth = FirebaseAuth.getInstance()
    private var friendHandle: String? = null
    
    // Limits
    private val MAX_SUBMISSIONS_PREVIEW = 5
    private var allSubmissions = listOf<com.example.codeforces.models.Submission>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = Color.parseColor("#182218") // surface_container
        window.navigationBarColor = Color.parseColor("#0A0A0A")
        
        binding = ActivityFriendProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        friendHandle = intent.getStringExtra("HANDLE")
        if (friendHandle == null) {
            Toast.makeText(this, "Handle missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Setup Toolbar
        binding.toolbar.title = friendHandle
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupListeners()

        loadFriendData()
    }

    private fun setupRecyclerView() {
        submissionsAdapter = SubmissionsAdapter()
        binding.recyclerSubmissions.apply {
            layoutManager = LinearLayoutManager(this@FriendProfileActivity)
            adapter = submissionsAdapter
        }
    }

    private fun setupListeners() {
        binding.btnProfileUnfriend.setOnClickListener {
            confirmRemoveFriend(friendHandle!!)
        }

        binding.btnProfileShare.setOnClickListener {
            val url = "https://codeforces.com/profile/$friendHandle"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check out $friendHandle on Codeforces: $url")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Friend"))
        }

        binding.btnViewAllSubmissions.setOnClickListener {
            if (binding.btnViewAllSubmissions.text.toString().contains("VIEW ALL")) {
                submissionsAdapter.submitList(allSubmissions)
                binding.btnViewAllSubmissions.text = "COLLAPSE ↑"
            } else {
                submissionsAdapter.submitList(allSubmissions.take(MAX_SUBMISSIONS_PREVIEW))
                binding.btnViewAllSubmissions.text = "VIEW ALL →"
            }
        }
    }

    private fun loadFriendData() {
        binding.progressProfile.visibility = View.VISIBLE
        binding.contentContainer.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                // Fetch user info
                val responseInfo = RetrofitInstance.api.getUserInfo(friendHandle!!)
                if (responseInfo.isSuccessful) {
                    val user = responseInfo.body()?.result?.firstOrNull()
                    if (user != null) {
                        populateUserInfo(user)
                    }
                }
                
                // Fetch recent submissions
                val responseStatus = RetrofitInstance.api.getUserSubmissions(friendHandle!!, count = 20)
                if (responseStatus.isSuccessful) {
                    val subs = responseStatus.body()?.result ?: emptyList()
                    allSubmissions = subs
                    
                    if (subs.isEmpty()) {
                        binding.tvEmptySubmissions.visibility = View.VISIBLE
                        binding.recyclerSubmissions.visibility = View.GONE
                        binding.btnViewAllSubmissions.visibility = View.GONE
                    } else {
                        binding.tvEmptySubmissions.visibility = View.GONE
                        binding.recyclerSubmissions.visibility = View.VISIBLE
                        submissionsAdapter.submitList(subs.take(MAX_SUBMISSIONS_PREVIEW))
                        
                        if (subs.size > MAX_SUBMISSIONS_PREVIEW) {
                            binding.btnViewAllSubmissions.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressProfile.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun populateUserInfo(user: com.example.codeforces.models.User) {
        val friendTheme = ThemeManager.getThemeForRating(this, user.rating)
        val friendColor = friendTheme.primary
        val friendSurface = friendTheme.surface
        val (rankLabel, _) = RankUtils.getRankInfo(user.rating)

        binding.profileFriendHandle.text = user.handle
        binding.profileFriendHandle.setTextColor(friendColor)
        
        binding.profileFriendRank.text = rankLabel.uppercase()
        binding.profileFriendRank.setTextColor(friendTheme.onPrimary)
        binding.profileFriendRank.backgroundTintList = android.content.res.ColorStateList.valueOf(friendTheme.primary)
        binding.profileAvatarContainer.setBackgroundColor(friendColor)
        
        binding.profileFriendRating.text = user.rating?.toString() ?: "UNRATED"
        binding.profileFriendRating.setTextColor(friendColor)

        if (!user.titlePhoto.isNullOrBlank()) {
            Glide.with(this)
                .load(user.titlePhoto)
                .placeholder(R.drawable.ic_profile)
                .centerCrop()
                .into(binding.profileFriendAvatar)
        }

        // Mock stats
        binding.profileFriendContributions.text = "+${Random.nextInt(10, 150)}"
        
        // Mock Head-to-head
        val myContests = Random.nextInt(5, 20)
        val friendContests = Random.nextInt(20, 100)
        binding.profileMyContests.text = myContests.toString()
        binding.profileFriendContests.text = friendContests.toString()
        
        val total = myContests + friendContests
        val myWeight = myContests.toFloat() / total
        val friendWeight = friendContests.toFloat() / total
        
        val paramsMy = binding.profileContestsMyBar.layoutParams as android.widget.LinearLayout.LayoutParams
        paramsMy.weight = myWeight
        binding.profileContestsMyBar.layoutParams = paramsMy
        
        val paramsFriend = binding.profileContestsFriendBar.layoutParams as android.widget.LinearLayout.LayoutParams
        paramsFriend.weight = friendWeight
        binding.profileContestsFriendBar.layoutParams = paramsFriend

        // Apply theme to the rest of the views
        binding.profileFriendContests.setTextColor(friendColor)
        binding.tvFriendContestsLabelHim.setTextColor(friendColor)
        binding.profileContestsFriendBar.setBackgroundColor(friendColor)
        binding.indicatorFriendRecentSubmissions.setBackgroundColor(friendColor)
        binding.btnViewAllSubmissions.setTextColor(friendColor)
        
        // Dynamic Card Backgrounds
        (binding.cardFriendRating.background?.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(friendSurface)
        (binding.cardFriendContributions.background?.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(friendSurface)
        (binding.cardFriendHeadToHead.background?.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(friendSurface)
        (binding.btnProfileShare.background?.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(friendSurface)
    }

    private fun confirmRemoveFriend(handle: String) {
        val uid = auth.currentUser?.uid ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("Unfriend")
            .setMessage("Are you sure you want to remove @$handle?")
            .setPositiveButton("REMOVE") { _, _ ->
                lifecycleScope.launch {
                    try {
                        UserRepository.removeFriend(uid, handle)
                        Toast.makeText(this@FriendProfileActivity, "Removed $handle", Toast.LENGTH_SHORT).show()
                        finish() // Return to friends list
                    } catch (e: Exception) {
                        Toast.makeText(this@FriendProfileActivity, "Error removing friend", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }
}
