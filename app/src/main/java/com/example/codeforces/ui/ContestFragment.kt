package com.example.codeforces.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.databinding.FragmentContestBinding
import com.example.codeforces.models.ApiResponse
import com.example.codeforces.models.Contest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContestFragment : Fragment() {

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

        binding.btnSort.setOnClickListener {
            showSortOptions()
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
                            adapter.submitList(contests)

                            // Show recycler view
                            binding.recyclerViewContests.visibility = View.VISIBLE
                        } else {
                            showError("No contests found")
                        }
                    } else {
                        showError("Failed to load contests (Code: ${response.code()})")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<Contest>>>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    binding.progressBarContests.visibility = View.GONE
                    showError("Network Error: ${t.message}")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            if (isAdded && _binding != null) {
                binding.progressBarContests.visibility = View.GONE
                showError("Unexpected Error: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        binding.recyclerViewContests.visibility = View.GONE
    }

    private fun showSortOptions() {
        val options = arrayOf("All", "Upcoming", "Ongoing", "Finished")

        AlertDialog.Builder(requireContext())
            .setTitle("Sort Contests")
            .setItems(options) { _, which ->
                val filtered = when (which) {
                    1 -> {
                        Toast.makeText(requireContext(), "Showing Upcoming Contests", Toast.LENGTH_SHORT).show()
                        allContests.filter { it.phase == "BEFORE" }    // Upcoming
                    }
                    2 -> {
                        Toast.makeText(requireContext(), "Showing Ongoing Contests", Toast.LENGTH_SHORT).show()
                        allContests.filter { it.phase == "CODING" }    // Ongoing
                    }
                    3 -> {
                        Toast.makeText(requireContext(), "Showing Finished Contests", Toast.LENGTH_SHORT).show()
                        allContests.filter { it.phase == "FINISHED" }  // Finished
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Showing All Contests", Toast.LENGTH_SHORT).show()
                        allContests // All
                    }
                }
                adapter.submitList(filtered)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
