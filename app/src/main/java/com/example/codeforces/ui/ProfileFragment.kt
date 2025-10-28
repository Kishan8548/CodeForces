package com.example.codeforces.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.codeforces.R
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.api.RetrofitInstance.api
import com.example.codeforces.databinding.FragmentProfileBinding
import com.example.codeforces.models.ApiResponse
import com.example.codeforces.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var currentUser: User? = null

    private var handle: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        binding.btnChangeHandle.setOnClickListener {
            val prefs = requireActivity().getSharedPreferences("CodeforcesPrefs", AppCompatActivity.MODE_PRIVATE)
            prefs.edit().remove("HANDLE").apply()

            val intent = Intent(requireContext(), EnterUsernameActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        handle = arguments?.getString("HANDLE")
        Log.d("ProfileFragment", "Handle: $handle")
        if (handle != null) {
            fetchProfileData(handle!!)
            fetchUserBlogs()
        } else {
            Toast.makeText(requireContext(), "No handle provided", Toast.LENGTH_SHORT).show()
        }

        binding.profileImage.setOnClickListener {
            val imageUrl = currentUser?.titlePhoto ?: return@setOnClickListener
            showFullScreenImage(imageUrl)
        }



    }

    private fun fetchProfileData(handle: String) {

        binding.progressBarProfile.visibility = View.VISIBLE
        binding.recyclerProblems.visibility = View.GONE

        val call = RetrofitInstance.api.getUserInfo(handle)
        call.enqueue(object : Callback<ApiResponse<List<User>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<User>>>,
                response: Response<ApiResponse<List<User>>>

            ) {




                binding.progressBarProfile.visibility = View.GONE
                if (response.isSuccessful && response.body()?.status == "OK") {
                    val user = response.body()?.result?.firstOrNull()
                    if (user != null) {
                        currentUser = user
                        binding.recyclerProblems.visibility = View.VISIBLE
                        updateUI(user)
                    }
                    else {
                        Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<User>>>, t: Throwable) {
                if (!isAdded || _binding == null) return
                binding.progressBarProfile.visibility = View.GONE

                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(user: User) {
        val (rankTitle, color) = getRankAndColor(user.rating)
        binding.tvRank.text = rankTitle
        binding.tvRank.setTextColor(color)
        with(binding) {
            tvHandle.text = user.handle
            tvRank.text = user.rank ?: "Unrated"
            tvFullName.text = user.firstName +" "+ user.lastName
            tvContestRating.text = "Contest rating: ${user.rating ?: 0} (max. ${user.maxRank ?: "-"}, ${user.maxRating ?: 0})"
            tvContribution.text = "Contribution: ${user.contribution ?: 0}"
            tvFriends.text = "Friend of: ${user.friendOfCount ?: 0} users"
            tvCity.text = "City: ${user.city ?: "N/A"}"
            tvCountry.text = "Country: ${user.country ?: "N/A"}"
            tvOrganization.text = "Organization: ${user.organization ?: "N/A"}"
            Glide.with(requireContext())
                .load("${user.titlePhoto}")
                .placeholder(R.drawable.ic_profile)
                .into(profileImage)
        }

    }

    private fun fetchUserBlogs() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getUserBlogEntries(handle).awaitResponse()
                }
                if (response.isSuccessful) {
                    val blogs = response.body()?.result ?: emptyList()
                    binding.tvBlogEntries.text = "Blogs: ${blogs.size}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun showFullScreenImage(imageUrl: String) {
        val inflater = LayoutInflater.from(requireContext())
        val overlay = inflater.inflate(R.layout.layout_fullscreen_image, null)

        val imageView = overlay.findViewById<ImageView>(R.id.fullscreenImageView)

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_profile)
            .into(imageView)
        val decorView = requireActivity().window.decorView as ViewGroup
        decorView.addView(overlay)

        overlay.setOnClickListener {
            decorView.removeView(overlay)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getRankAndColor(rating: Int?): Pair<String, Int> {
        val r = rating ?: 0
        val context = requireContext()

        return when {
            r < 1200 -> "Newbie" to context.getColor(R.color.newbie)
            r < 1400 -> "Pupil" to context.getColor(R.color.pupil)
            r < 1600 -> "Specialist" to context.getColor(R.color.specialist)
            r < 1900 -> "Expert" to context.getColor(R.color.expert)
            r < 2100 -> "Candidate Master" to context.getColor(R.color.candidate_master)
            r < 2300 -> "Master" to context.getColor(R.color.master)
            r < 2400 -> "International Master" to context.getColor(R.color.international_master)
            r < 2600 -> "Grandmaster" to context.getColor(R.color.grandmaster)
            r < 3000 -> "International Grandmaster" to context.getColor(R.color.international_grandmaster)
            else -> "Legendary Grandmaster" to context.getColor(R.color.legendary_grandmaster)
        }
    }


}
