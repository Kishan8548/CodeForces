package com.example.codeforces.ui
import com.google.android.material.chip.Chip
import androidx.core.view.children

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
import com.example.codeforces.models.ApiResponse
import com.example.codeforces.models.Problem
import com.example.codeforces.models.ProblemSetResponse
import com.example.codeforces.models.ProblemStatistics
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProblemsFragment : Fragment() {
    private fun setupChips() {
        val tags = resources.getStringArray(R.array.problem_tags)

        for (tag in tags) {
            val chip = Chip(requireContext()).apply {
                text = tag
                isCheckable = true


                setOnCheckedChangeListener { _, _ ->
                    applyFilters()
                }
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


        adapter = ProblemsAdapter(emptyList(), emptyList()) // initially empty
        binding.recyclerProblems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProblems.adapter = adapter

        val sortOptions = listOf("Newest First", "Oldest First")
        binding.spinnerSortTime.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortOptions


        )

        setupChips()
        fetchProblems()

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchProblems()
        }

        binding.btnApplyRatingFilter.setOnClickListener {
            applyFilters()
        }

        binding.spinnerSortTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                sortProblems()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun fetchProblems() {
        // Show loader immediately
        binding.progressBarContests.visibility = View.VISIBLE
        binding.recyclerProblems.visibility = View.GONE

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
                    allStatistics = result.problemStatistics
                    filteredProblems = allProblems

                    adapter = ProblemsAdapter(filteredProblems, allStatistics)
                    binding.recyclerProblems.adapter = adapter

                } else {
                    Toast.makeText(requireContext(), "Failed to load problems", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {

                if (!isAdded || _binding == null) return@launch

                binding.progressBarContests.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
