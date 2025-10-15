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
        binding.progressBarContests.visibility = View.VISIBLE

        RetrofitInstance.api.getContestList().enqueue(object : Callback<ApiResponse<List<Contest>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Contest>>>,
                response: Response<ApiResponse<List<Contest>>>
            ) {
                binding.progressBarContests.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val contests = response.body()!!.result ?: emptyList()
                    allContests.clear()
                    allContests.addAll(contests) // keep full list for sorting
                    adapter.submitList(contests)
                } else {
                    Toast.makeText(requireContext(), "Failed to load contests", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Contest>>>, t: Throwable) {
                binding.progressBarContests.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
                        allContests                                 // All
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
