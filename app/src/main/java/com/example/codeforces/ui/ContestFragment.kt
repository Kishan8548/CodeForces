package com.example.codeforces.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.databinding.FragmentContestBinding
import com.example.codeforces.models.ApiResponse
import com.example.codeforces.models.Contest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

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

        // Force dropdown to show on click
        binding.dropdownSort.setOnClickListener {
            binding.dropdownSort.showDropDown()
        }

        // Default value
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
        // ✅ Step 1: Show loader, hide list
        binding.progressBarContests.visibility = View.VISIBLE
        binding.recyclerViewContests.visibility = View.GONE
        binding.emptyView.visibility = View.GONE

        try {
            RetrofitInstance.api.getContestList().enqueue(object : Callback<ApiResponse<List<Contest>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Contest>>>,
                    response: Response<ApiResponse<List<Contest>>>
                ) {
                    // ✅ Step 2: Prevent crash if fragment not attached
                    if (!isAdded || _binding == null) return

                    // ✅ Step 3: Hide loader after response
                    binding.progressBarContests.visibility = View.GONE

                    if (response.isSuccessful) {
                        val contests = response.body()?.result

                        if (!contests.isNullOrEmpty()) {
                            // ✅ Step 4: Update adapter safely
                            allContests.clear()
                            allContests.addAll(contests)
                            applyFilter(currentFilter)
                            adapter.submitList(contests.sortedBy { it.startTimeSeconds ?: 0 })

                            // Show recycler view
                            binding.recyclerViewContests.visibility = View.VISIBLE
                            binding.emptyView.visibility = View.GONE
                        } else {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.recyclerViewContests.visibility = View.GONE
                        }
                    } else {
                        showError("Failed to load contests (Code: ${response.code()})")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<Contest>>>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    binding.progressBarContests.visibility = View.GONE

                    binding.emptyView.visibility = View.VISIBLE
                    binding.recyclerViewContests.visibility = View.GONE

                    showError("Network Error: ${t.message}")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            if (isAdded && _binding != null) {
                binding.progressBarContests.visibility = View.GONE

                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerViewContests.visibility = View.GONE

                showError("Unexpected Error: ${e.message}")
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

        adapter.submitList(filteredList.toList())

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
