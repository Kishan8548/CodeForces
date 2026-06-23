package com.example.codeforces.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.codeforces.databinding.ItemSubmissionBinding
import com.example.codeforces.models.Submission

class SubmissionsAdapter : RecyclerView.Adapter<SubmissionsAdapter.ViewHolder>() {

    private val submissions = mutableListOf<Submission>()

    inner class ViewHolder(val binding: ItemSubmissionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubmissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sub = submissions[position]
        holder.binding.apply {
            tvProblemId.text = "${sub.problem.contestId}${sub.problem.index}"
            tvProblemName.text = sub.problem.name

            val isAccepted = sub.verdict == "OK"
            val verdictText = if (isAccepted) "ACCEPTED" else sub.verdict?.replace("_", " ") ?: "UNKNOWN"
            
            tvVerdict.text = verdictText
            if (isAccepted) {
                tvVerdict.setTextColor(Color.parseColor("#00ff66"))
                tvVerdict.setBackgroundColor(Color.parseColor("#3300ff66"))
            } else {
                tvVerdict.setTextColor(Color.parseColor("#ffb4ab"))
                tvVerdict.setBackgroundColor(Color.parseColor("#33ffb4ab"))
            }

            tvTime.text = "${sub.timeConsumedMillis} ms"
        }
    }

    override fun getItemCount() = submissions.size

    fun submitList(list: List<Submission>) {
        submissions.clear()
        submissions.addAll(list)
        notifyDataSetChanged()
    }
}
