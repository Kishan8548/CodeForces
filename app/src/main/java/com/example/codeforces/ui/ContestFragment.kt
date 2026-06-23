package com.example.codeforces.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codeforces.adapter.ContestAdapter
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.databinding.FragmentContestBinding
import com.example.codeforces.models.Contest
import com.example.codeforces.utils.ThemeManager
import com.example.codeforces.utils.DataCache
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ContestFragment : Fragment() {

    private var _binding: FragmentContestBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContestAdapter
    private val allContests = mutableListOf<Contest>()
    private var currentFilter = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
        setupRecyclerView()
        setupFilterSpinner()
        
        binding.swipeRefreshContest.setOnRefreshListener {
            loadData(force = true)
        }
        loadData(force = false)
    }

    // ─── Theme ───────────────────────────────────────────────────────────────

    private fun applyTheme() {
        // Spinner text color styling is handled via ArrayAdapter
    }

    // ─── Filter Spinner ──────────────────────────────────────────────────────

    private fun setupFilterSpinner() {
        val options = arrayOf("All", "Upcoming", "Ongoing", "Finished")

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.dropdownSort.adapter = spinnerAdapter
        binding.dropdownSort.setSelection(0, false)

        binding.dropdownSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (allContests.isEmpty()) return
                currentFilter = position
                applyFilter(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // ─── RecyclerView ────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = ContestAdapter()
        binding.recyclerViewContests.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewContests.adapter = adapter
    }

    // ─── API Fetch ───────────────────────────────────────────────────────────

    private fun loadData(force: Boolean) {
        if (!force && DataCache.cachedContests != null) {
            allContests.clear()
            allContests.addAll(DataCache.cachedContests!!)
            applyFilter(currentFilter)
            binding.swipeRefreshContest.isRefreshing = false
        } else {
            fetchContestList()
        }
    }

    private fun fetchContestList() {
        if (!binding.swipeRefreshContest.isRefreshing) {
            binding.shimmerContests.startShimmer()
            binding.shimmerContests.visibility = View.VISIBLE
            binding.recyclerViewContests.visibility = View.GONE
        }
        binding.emptyView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getContestList()

                if (!isAdded || _binding == null) return@launch

                binding.shimmerContests.stopShimmer()
                binding.shimmerContests.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == "OK") {
                    val data = response.body()?.result ?: emptyList()
                    allContests.clear()
                    allContests.addAll(data)
                    DataCache.cachedContests = allContests.toList()
                    applyFilter(currentFilter)
                    binding.recyclerViewContests.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                } else {
                    showError("Failed to load contests (${response.code()})")
                }

            } catch (e: Exception) {
                if (!isAdded || _binding == null) return@launch
                binding.shimmerContests.stopShimmer()
                binding.shimmerContests.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.recyclerViewContests.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            } finally {
                _binding?.swipeRefreshContest?.isRefreshing = false
            }
        }
    }

    // ─── Filter logic ────────────────────────────────────────────────────────

    private fun applyFilter(position: Int) {
        val filtered = when (position) {
            1 -> allContests.filter { it.phase == "BEFORE" }           // Upcoming
            2 -> allContests.filter { it.phase == "CODING" }           // Ongoing
            3 -> allContests.filter { it.phase == "FINISHED" }         // Finished
            else -> allContests                                          // All
        }

        adapter.submitList(filtered)

        if (filtered.isEmpty()) {
            binding.recyclerViewContests.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.recyclerViewContests.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        binding.recyclerViewContests.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}