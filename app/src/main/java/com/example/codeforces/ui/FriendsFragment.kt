package com.example.codeforces.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.codeforces.R
import com.example.codeforces.adapter.FriendsAdapter
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.databinding.FragmentFriendsBinding
import com.example.codeforces.models.User
import com.example.codeforces.repository.UserRepository
import com.example.codeforces.utils.DataCache
import com.example.codeforces.utils.RankUtils
import com.example.codeforces.utils.ThemeManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: FriendsAdapter

    private var searchJob: Job? = null
    private var allFriendsCache = listOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
        setupRecyclerView()
        setupSearch()

        if (auth.currentUser == null) {
            showSignInRequired()
        } else {
            binding.swipeRefreshFriends.setOnRefreshListener { loadData(force = true) }
            loadData(force = false)
        }

        binding.btnGoSignIn.setOnClickListener {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }
    }

    // ─── Theme ───────────────────────────────────────────────────────────────

    private fun applyTheme() {
        val theme = ThemeManager.current
        binding.btnGoSignIn.backgroundTintList = ColorStateList.valueOf(theme.primary)
        binding.btnGoSignIn.setTextColor(theme.onPrimary)
        binding.progressFriends.indeterminateTintList = ColorStateList.valueOf(theme.primary)
        
        // Dynamic Text Colors and Indicators
        binding.tvTotalFriends.setTextColor(theme.primary)
        binding.tvActiveNow.setTextColor(theme.primary)
        binding.indicatorActiveNow.setBackgroundColor(theme.primary)
    }

    // ─── State management ────────────────────────────────────────────────────

    private fun showSignInRequired() {
        binding.cardSignInRequired.visibility = View.VISIBLE
        binding.recyclerFriends.visibility = View.GONE
        binding.emptyFriends.visibility = View.GONE
        binding.progressFriends.visibility = View.GONE
        binding.layoutStatsRow.visibility = View.GONE
        binding.tvFriendsHeader.visibility = View.GONE
        binding.editSearchFriends.isEnabled = false
    }

    private fun showLoading() {
        binding.progressFriends.visibility = View.VISIBLE
        binding.recyclerFriends.visibility = View.GONE
        binding.emptyFriends.visibility = View.GONE
        binding.cardSignInRequired.visibility = View.GONE
    }

    private fun showList(hasItems: Boolean, isSearch: Boolean = false) {
        binding.progressFriends.visibility = View.GONE
        binding.cardSignInRequired.visibility = View.GONE
        binding.recyclerFriends.visibility = if (hasItems) View.VISIBLE else View.GONE
        binding.emptyFriends.visibility = if (hasItems) View.GONE else View.VISIBLE
        
        if (isSearch) {
            binding.tvFriendsHeader.text = "SEARCH RESULTS"
        } else {
            binding.tvFriendsHeader.text = "MY FRIENDS"
        }
    }

    private fun updateStats(count: Int) {
        binding.tvTotalFriends.text = count.toString()
        val active = if (count == 0) 0 else Random.nextInt(1, (count + 1).coerceAtMost(20))
        binding.tvActiveNow.text = active.toString()
    }

    // ─── Search Bar ─────────────────────────────────────────────────────────

    private fun setupSearch() {
        binding.editSearchFriends.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                binding.btnCancelSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                
                searchJob?.cancel()
                if (query.isEmpty()) {
                    adapter.submitList(allFriendsCache, false)
                    showList(allFriendsCache.isNotEmpty(), false)
                } else {
                    searchJob = lifecycleScope.launch {
                        delay(600) // debounce
                        performSearch(query)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnCancelSearch.setOnClickListener {
            binding.editSearchFriends.text.clear()
            binding.editSearchFriends.clearFocus()
        }
        
        binding.editSearchFriends.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.editSearchFriends.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchJob?.cancel()
                    performSearch(query)
                }
                true
            } else false
        }
    }

    private fun performSearch(handle: String) {
        showLoading()
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getUserInfo(handle)
                if (response.isSuccessful && response.body()?.status == "OK") {
                    val user = response.body()?.result?.firstOrNull()
                    if (user != null) {
                        adapter.submitList(listOf(user), true)
                        showList(true, true)
                        return@launch
                    }
                }
                showList(false, true)
            } catch (e: Exception) {
                showList(false, true)
            }
        }
    }

    // ─── RecyclerView ────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = FriendsAdapter(
            onClick = { user -> showFriendBottomSheet(user) },
            onAddFriend = { user -> 
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    addFriend(uid, user.handle)
                    binding.editSearchFriends.text.clear()
                }
            }
        )
        binding.recyclerFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFriends.adapter = adapter
    }

    // ─── Load friends from Firestore → fetch CF info for each ────────────────

    private fun loadData(force: Boolean) {
        if (!force && DataCache.cachedFriends != null) {
            allFriendsCache = DataCache.cachedFriends!!
            updateStats(allFriendsCache.size)
            if (binding.editSearchFriends.text.toString().trim().isEmpty()) {
                adapter.submitList(allFriendsCache, false)
                showList(allFriendsCache.isNotEmpty(), false)
            }
            binding.swipeRefreshFriends.isRefreshing = false
        } else {
            loadFriends()
        }
    }

    private fun loadFriends() {
        val uid = auth.currentUser?.uid ?: return
        if (!binding.swipeRefreshFriends.isRefreshing) {
            showLoading()
        }

        lifecycleScope.launch {
            val handles = UserRepository.getFriends(uid)
            if (!isAdded || _binding == null) return@launch

            if (handles.isEmpty()) {
                allFriendsCache = emptyList()
                DataCache.cachedFriends = allFriendsCache
                updateStats(0)
                showList(false, false)
                binding.swipeRefreshFriends.isRefreshing = false
                return@launch
            }

            // Fetch CF user info for each friend
            val friendUsers = mutableListOf<com.example.codeforces.models.User>()
            for (handle in handles) {
                try {
                    val response = RetrofitInstance.api.getUserInfo(handle)
                    if (response.isSuccessful) {
                        response.body()?.result?.firstOrNull()?.let { friendUsers.add(it) }
                    }
                } catch (_: Exception) {}
            }

            if (!isAdded || _binding == null) {
                _binding?.swipeRefreshFriends?.isRefreshing = false
                return@launch
            }
            
            allFriendsCache = friendUsers
            DataCache.cachedFriends = allFriendsCache
            updateStats(allFriendsCache.size)
            
            // Only submit if we are not currently searching
            if (binding.editSearchFriends.text.toString().trim().isEmpty()) {
                adapter.submitList(allFriendsCache, false)
                showList(allFriendsCache.isNotEmpty(), false)
            }
            binding.swipeRefreshFriends.isRefreshing = false
        }
    }

    private fun showFriendBottomSheet(user: User) {
        val intent = Intent(requireContext(), FriendProfileActivity::class.java)
        intent.putExtra("HANDLE", user.handle)
        startActivity(intent)
    }

    private fun addFriend(uid: String, handle: String) {
        lifecycleScope.launch {
            try {
                UserRepository.addFriend(uid, handle)
                if (isAdded) Toast.makeText(requireContext(), "✓ $handle added!", Toast.LENGTH_SHORT).show()
                loadData(force = true)
            } catch (e: Exception) {
                if (isAdded) Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmRemoveFriend(handle: String) {
        val uid = auth.currentUser?.uid ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unfriend")
            .setMessage("Are you sure you want to remove @$handle?")
            .setPositiveButton("REMOVE") { _, _ ->
                lifecycleScope.launch {
                    UserRepository.removeFriend(uid, handle)
                    if (isAdded) Toast.makeText(requireContext(), "Removed $handle", Toast.LENGTH_SHORT).show()
                    loadData(force = true)
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
