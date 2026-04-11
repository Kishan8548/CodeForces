package com.example.codeforces.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.databinding.FragmentContestBinding
import com.example.codeforces.models.Contest
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ContestFragment : Fragment() {

    private var currentFilter = 0 // 0 = All, 1 = Upcoming, 2 = Ongoing, 3 = Finished
    private var _binding: FragmentContestBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContestAdapter
    private val allContests = mutableListOf<Contest>()

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
        setupRecyclerView()
        fetchContestList()
        setupDropdown()
    }

    private fun setupDropdown() {
        val options = arrayOf("All", "Upcoming", "Ongoing", "Finished")

        val adapterDropdown = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            options
        )

        binding.dropdownSort.setAdapter(adapterDropdown)

        binding.dropdownSort.setOnClickListener {
            binding.dropdownSort.showDropDown()
        }

        binding.dropdownSort.setText(options[0], false)

        binding.dropdownSort.setOnItemClickListener { _, _, position, _ ->
            applyFilter(position)
        }
    }

    private fun setupRecyclerView() {
        adapter = ContestAdapter()
        binding.recyclerViewContests.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewContests.adapter = adapter
    }

    private fun fetchContestList() {
        // Show shimmer loader
        binding.shimmerContests.startShimmer()
        binding.shimmerContests.visibility = View.VISIBLE
        binding.recyclerViewContests.visibility = View.GONE
        binding.emptyView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getContestList()

                if (!isAdded || _binding == null) return@launch

                // Hide shimmer
                binding.shimmerContests.stopShimmer()
                binding.shimmerContests.visibility = View.GONE

                if (response.isSuccessful) {
                    val contests = response.body()?.result

                    if (!contests.isNullOrEmpty()) {
                        allContests.clear()
                        allContests.addAll(contests)

                        applyFilter(currentFilter)

                        binding.recyclerViewContests.visibility = View.VISIBLE
                        binding.emptyView.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.recyclerViewContests.visibility = View.GONE
                    }
                } else {
                    showError("Failed to load contests (Code: ${response.code()})")
                }

            } catch (e: Exception) {
                if (!isAdded || _binding == null) return@launch

                binding.shimmerContests.stopShimmer()
                binding.shimmerContests.visibility = View.GONE

                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerViewContests.visibility = View.GONE

                showError("Network Error: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        binding.recyclerViewContests.visibility = View.GONE
    }

    private fun applyFilter(position: Int) {
        currentFilter = position

        val filteredList = when (position) {
            1 -> allContests.filter { it.phase == "BEFORE" }
            2 -> allContests.filter { it.phase == "CODING" }
            3 -> allContests.filter { it.phase == "FINISHED" }
            else -> allContests
        }

        adapter.submitList(filteredList)

        val message = when (position) {
            1 -> "Showing Upcoming Contests"
            2 -> "Showing Ongoing Contests"
            3 -> "Showing Finished Contests"
            else -> "Showing All Contests"
        }

        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}