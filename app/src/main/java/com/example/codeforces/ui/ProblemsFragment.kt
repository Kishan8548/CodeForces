package com.example.codeforces.ui
import com.google.android.material.chip.Chip
import androidx.core.view.children

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
import com.example.codeforces.R
import com.example.codeforces.adapter.ProblemsAdapter
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.databinding.FragmentProblemsBinding
import com.example.codeforces.models.Problem
import com.example.codeforces.models.ProblemStatistics
import com.example.codeforces.utils.ThemeManager
import com.example.codeforces.utils.DataCache
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProblemsFragment : Fragment() {
    private fun setupChips() {
        val tags = resources.getStringArray(R.array.problem_tags)
        val theme = ThemeManager.current

        for (tag in tags) {
            val chip = Chip(requireContext()).apply {
                text = tag
                isCheckable = true
                setTextColor(ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(theme.onPrimary, theme.onSurfaceVariant)
                ))
                chipBackgroundColor = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                    intArrayOf(theme.primary, theme.surface)
                )
                chipStrokeColor = ColorStateList.valueOf(theme.strokeGray)
                chipStrokeWidth = 1f

                setOnCheckedChangeListener { _, _ -> applyFilters() }
            }
            binding.chipGroupTags.addView(chip)
        }
    }

    private var _binding: FragmentProblemsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProblemsAdapter
    private var allProblems: List<Problem> = emptyList()
    private var filteredProblems: List<Problem> = emptyList()
    private var allStatistics: List<ProblemStatistics> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProblemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply rank theme
        applyRankTheme()

        adapter = ProblemsAdapter(emptyList(), emptyList())
        binding.recyclerProblems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProblems.adapter = adapter

        val sortOptions = listOf("Newest First", "Oldest First")
        binding.spinnerSortTime.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortOptions
        )

        setupChips()
        
        binding.swipeRefreshLayout.setOnRefreshListener { loadData(force = true) }
        loadData(force = false)

        binding.btnApplyRatingFilter.setOnClickListener { applyFilters() }

        binding.spinnerSortTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sortProblems()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Toggle Filter Minimize/Expand
        binding.btnMinimizeFilter.setOnClickListener {
            binding.layoutFilterContent.visibility = View.GONE
            binding.btnExpandFilter.visibility = View.VISIBLE
            binding.btnMinimizeFilter.visibility = View.GONE
        }

        binding.btnExpandFilter.setOnClickListener {
            binding.layoutFilterContent.visibility = View.VISIBLE
            binding.btnExpandFilter.visibility = View.GONE
            binding.btnMinimizeFilter.visibility = View.VISIBLE
        }
    }

    private fun applyRankTheme() {
        val theme = ThemeManager.current
        // Apply button + hard shadow
        binding.btnApplyRatingFilter.backgroundTintList = ColorStateList.valueOf(theme.primary)
        binding.btnApplyRatingFilter.setTextColor(theme.onPrimary)
        binding.btnApplyShadow.setBackgroundColor(theme.primaryDim)

        // Dynamic Text Colors
        binding.tvFilterByRating.setTextColor(theme.primary)
        binding.btnExpandFilter.setTextColor(theme.primary)
        binding.tvViewAllTags.setTextColor(theme.primary)
        binding.btnMinimizeFilter.setTextColor(theme.primary)
        // Progress bar
        binding.progressBarContests.indeterminateTintList = ColorStateList.valueOf(theme.primary)
        // Swipe refresh
        binding.swipeRefreshLayout.setColorSchemeColors(theme.primary)
    }

    // ─── API Fetch ───────────────────────────────────────────────────────────

    private fun loadData(force: Boolean) {
        if (!force && DataCache.cachedProblems != null) {
            allProblems = DataCache.cachedProblems!!
            allStatistics = DataCache.cachedStatistics!!
            applyFilters()
            binding.swipeRefreshLayout.isRefreshing = false
        } else {
            fetchProblems()
        }
    }

    private fun fetchProblems() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBarContests.visibility = View.VISIBLE
            binding.recyclerProblems.visibility = View.GONE
        }

        lifecycleScope.launch {

            try {
                val response = RetrofitInstance.api.getProblemSet()

                if (!isAdded || _binding == null) return@launch

                binding.progressBarContests.visibility = View.GONE

                binding.swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful && response.body()?.result != null) {

                    binding.recyclerProblems.visibility = View.VISIBLE

                    val result = response.body()!!.result
                    allProblems = result.problems
                    DataCache.cachedProblems = allProblems
                    allStatistics = result.problemStatistics
                    DataCache.cachedStatistics = allStatistics
                    
                    applyFilters()
                } else {
                    Toast.makeText(requireContext(), "Failed to load problems", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {

                if (!isAdded || _binding == null) return@launch

                binding.progressBarContests.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _binding?.swipeRefreshLayout?.isRefreshing = false
                _binding?.progressBarContests?.visibility = View.GONE
            }
        }
    }




    private fun applyFilters() {
        val minRating = binding.editMinRating.text.toString().toIntOrNull() ?: Int.MIN_VALUE
        val maxRating = binding.editMaxRating.text.toString().toIntOrNull() ?: Int.MAX_VALUE
        val selectedTags = getSelectedTags()

        filteredProblems = allProblems.filter { problem ->
            val ratingMatch = (problem.rating ?: 0) in minRating..maxRating
            val tagMatch =
                if (selectedTags.isEmpty()) true else problem.tags.any { it in selectedTags }
            ratingMatch && tagMatch
        }

        sortProblems()
    }

    private fun getSelectedTags(): List<String> {
        return binding.chipGroupTags.children
            .filterIsInstance<Chip>()
            .filter { it.isChecked }
            .map { it.text.toString() }
            .toList()
    }
    private fun sortProblems() {
        filteredProblems = when (binding.spinnerSortTime.selectedItem?.toString()) {
            "Newest First" -> filteredProblems.sortedByDescending { it.contestId }
            "Oldest First" -> filteredProblems.sortedBy { it.contestId }
            else -> filteredProblems
        }

        adapter.updateList(filteredProblems,allStatistics)
        binding.recyclerProblems.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
