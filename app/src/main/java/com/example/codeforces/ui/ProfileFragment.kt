package com.example.codeforces.ui

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.codeforces.R
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.api.RetrofitInstance.api
import com.example.codeforces.databinding.FragmentProfileBinding
import com.example.codeforces.models.User
import com.example.codeforces.utils.RankUtils
import com.example.codeforces.utils.ThemeManager
import com.example.codeforces.utils.DataCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var currentUser: User? = null
    private var handle: String? = null
    
    private lateinit var submissionsAdapter: com.example.codeforces.adapter.SubmissionsAdapter
    private var allSubmissions = listOf<com.example.codeforces.models.Submission>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        binding.btnChangeHandle.setOnClickListener {
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                // Authenticated — offer change handle or sign out
                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Account Options")
                    .setItems(arrayOf("Change Handle", "Sign Out")) { _, which ->
                        when (which) {
                            0 -> {
                                // Go to handle binding to re-enter CF handle
                                startActivity(Intent(requireContext(), HandleBindingActivity::class.java))
                            }
                            1 -> {
                                // Sign out
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                val prefs = requireActivity().getSharedPreferences("CodeforcesPrefs", AppCompatActivity.MODE_PRIVATE)
                                prefs.edit().remove("HANDLE").apply()
                                ThemeManager.reset()
                                startActivity(Intent(requireContext(), AuthActivity::class.java))
                                requireActivity().finish()
                            }
                        }
                    }
                    .show()
            } else {
                // Anonymous user — just clear handle and restart
                val prefs = requireActivity().getSharedPreferences("CodeforcesPrefs", AppCompatActivity.MODE_PRIVATE)
                prefs.edit().remove("HANDLE").apply()
                startActivity(Intent(requireContext(), EnterUsernameActivity::class.java))
                requireActivity().finish()
            }
        }

        val prefs = requireActivity().getSharedPreferences("CodeforcesPrefs", AppCompatActivity.MODE_PRIVATE)
        handle = prefs.getString("HANDLE", null)

        if (handle != null) {
            binding.swipeRefreshProfile.setOnRefreshListener {
                loadData(handle!!, force = true)
            }
            loadData(handle!!, force = false)
        } else {
            Toast.makeText(requireContext(), "No handle provided", Toast.LENGTH_SHORT).show()
        }

        submissionsAdapter = com.example.codeforces.adapter.SubmissionsAdapter()
        binding.recyclerSubmissions.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = submissionsAdapter
        }

        binding.btnViewAllSubmissions.setOnClickListener {
            if (binding.btnViewAllSubmissions.text.toString().contains("VIEW ALL")) {
                submissionsAdapter.submitList(allSubmissions)
                binding.btnViewAllSubmissions.text = "COLLAPSE ↑"
            } else {
                submissionsAdapter.submitList(allSubmissions.take(5))
                binding.btnViewAllSubmissions.text = "VIEW ALL →"
            }
        }

        // Tap avatar → fullscreen
        binding.profileImage.setOnClickListener {
            val url = currentUser?.titlePhoto ?: return@setOnClickListener
            showFullScreenImage(url)
        }
    }

    // ─── Data Fetch ──────────────────────────────────────────────────────────

    private fun loadData(handle: String, force: Boolean) {
        if (!force && DataCache.cachedUser != null) {
            currentUser = DataCache.cachedUser
            updateUI(currentUser!!)
            
            allSubmissions = DataCache.cachedSubmissions ?: emptyList()
            if (allSubmissions.isNotEmpty()) {
                binding.tvEmptySubmissions.visibility = View.GONE
                binding.recyclerSubmissions.visibility = View.VISIBLE
                submissionsAdapter.submitList(allSubmissions.take(5))
                if (allSubmissions.size > 5) binding.btnViewAllSubmissions.visibility = View.VISIBLE
            } else {
                binding.tvEmptySubmissions.visibility = View.VISIBLE
                binding.btnViewAllSubmissions.visibility = View.GONE
            }
            binding.swipeRefreshProfile.isRefreshing = false
        } else {
            binding.swipeRefreshProfile.isRefreshing = true
            fetchProfileData(handle)
            fetchUserBlogs()
            fetchRecentSubmissions(handle)
        }
    }

    private fun fetchProfileData(handle: String) {
        binding.progressBarProfile.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getUserInfo(handle)
                if (!isAdded || _binding == null) return@launch

                binding.progressBarProfile.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == "OK") {
                    val user = response.body()?.result?.firstOrNull()
                    if (user != null) {
                        currentUser = user
                        DataCache.cachedUser = user
                        updateUI(user)
                    } else {
                        Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (!isAdded || _binding == null) return@launch
                binding.progressBarProfile.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserBlogs() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getUserBlogEntries(handle).awaitResponse()
                }
                if (response.isSuccessful) {
                    val count = response.body()?.result?.size ?: 0
                    if (_binding != null) binding.tvBlogEntries.text = count.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchRecentSubmissions(handle: String) {
        binding.progressSubmissions.visibility = View.VISIBLE
        binding.recyclerSubmissions.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getUserSubmissions(handle, count = 20)
                if (!isAdded || _binding == null) return@launch
                
                if (response.isSuccessful) {
                    val subs = response.body()?.result ?: emptyList()
                    allSubmissions = subs
                    DataCache.cachedSubmissions = subs
                    
                    if (subs.isEmpty()) {
                        binding.tvEmptySubmissions.visibility = View.VISIBLE
                        binding.btnViewAllSubmissions.visibility = View.GONE
                    } else {
                        binding.tvEmptySubmissions.visibility = View.GONE
                        binding.recyclerSubmissions.visibility = View.VISIBLE
                        submissionsAdapter.submitList(subs.take(5))
                        
                        if (subs.size > 5) {
                            binding.btnViewAllSubmissions.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            } finally {
                _binding?.progressSubmissions?.visibility = View.GONE
                _binding?.swipeRefreshProfile?.isRefreshing = false
            }
        }
    }

    // ─── UI Update + Dynamic Theming ─────────────────────────────────────────

    private fun updateUI(user: User) {
        // 1. Apply global rank theme (sets ThemeManager.current)
        val (rankLabel, _) = RankUtils.applyTheme(requireContext(), user.rating)
        val theme = ThemeManager.current

        // 2. Basic text fields
        with(binding) {
            tvHandle.text = user.handle
            tvHandle.setTextColor(theme.primary)

            tvFullName.text = buildString {
                val fn = user.firstName ?: ""
                val ln = user.lastName ?: ""
                val full = "$fn $ln".trim()
                if (full.isNotBlank()) append(full)
            }

            tvRank.text = rankLabel.uppercase()

            // Rank number
            tvContestRating.text = (user.rating ?: 0).toString()
            tvContestRating.setTextColor(theme.primary)

            // Max rating sub-label
            tvMaxRating.text = "MAX ${user.maxRating ?: "—"}"

            tvContribution.text = (user.contribution ?: 0).toString()
            tvFriends.text = (user.friendOfCount ?: 0).toString()

            tvCity.text = user.city?.takeIf { it.isNotBlank() } ?: "N/A"
            tvCountry.text = user.country?.takeIf { it.isNotBlank() } ?: "N/A"
            tvOrganization.text = user.organization?.takeIf { it.isNotBlank() } ?: "N/A"

            // 3. Avatar ring → rank color
            val ringDrawable = (avatarRing.background as? GradientDrawable)
                ?: ContextCompat.getDrawable(requireContext(), R.drawable.bg_avatar_ring_dark)?.mutate() as? GradientDrawable
            ringDrawable?.setStroke(6, theme.primary)
            avatarRing.background = ringDrawable

            // 4. Rank badge → rank accent bg + on-primary text
            tvRank.backgroundTintList = android.content.res.ColorStateList.valueOf(theme.primary)
            tvRank.setTextColor(theme.onPrimary)

            // 5. Rating star icon color
            ivRatingStar.setTextColor(theme.primary)

            // 6. Detail section info icon was removed

            // 7. Change Handle button + shadow
            btnChangeHandle.backgroundTintList = android.content.res.ColorStateList.valueOf(theme.primary)
            btnChangeHandle.setTextColor(theme.onPrimary)
            btnShadow.setBackgroundColor(theme.primaryDim)

            indicatorRecentSubmissions.setBackgroundColor(theme.primary)
            btnViewAllSubmissions.setTextColor(theme.primary)

            // Dynamic Card Backgrounds
            (cardRating.background?.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(theme.surface)
            (cardContribution.background?.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(theme.surface)
            (cardFriends.background?.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(theme.surface)
            (cardProfileDetails.background?.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(theme.surface)

            // 8. Progress bar tint
            progressBarProfile.indeterminateTintList = android.content.res.ColorStateList.valueOf(theme.primary)

            // 9. Online pulse dot
            val dotDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_avatar_ring_dark)?.mutate() as? GradientDrawable
            dotDrawable?.setColor(theme.primary)
            dotDrawable?.cornerRadius = 100f
            onlineDot.background = dotDrawable

            // 10. Load avatar from CF
            Glide.with(requireContext())
                .load(user.titlePhoto)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(profileImage)
        }

        // 11. Update bottom nav accent color to match rank theme
        (activity as? MainActivity)?.applyRankTheme(theme.primary)
    }

    // ─── Fullscreen Image Overlay ────────────────────────────────────────────

    private fun showFullScreenImage(imageUrl: String) {
        val overlay = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_fullscreen_image, null)
        val imageView = overlay.findViewById<ImageView>(R.id.fullscreenImageView)
        Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_profile).into(imageView)
        val decorView = requireActivity().window.decorView as ViewGroup
        decorView.addView(overlay)
        overlay.setOnClickListener { decorView.removeView(overlay) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
