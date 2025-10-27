package com.example.codeforces.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.codeforces.R
import com.example.codeforces.models.Problem
import com.example.codeforces.models.ProblemStatistics

class ProblemsAdapter(
    private var list: List<Problem>,
    private val statistics: List<ProblemStatistics>
) : RecyclerView.Adapter<ProblemsAdapter.ProblemViewHolder>() {

    private val solvedMap = statistics.associateBy({ it.contestId to it.index }, { it.solvedCount })

    inner class ProblemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textTitle)
        val index: TextView = view.findViewById(R.id.textIndex)
        val rating: TextView = view.findViewById(R.id.textRating)
        val tags: TextView = view.findViewById(R.id.textTags)
        val solved: TextView = view.findViewById(R.id.textSolved)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.problem_item, parent, false)
        return ProblemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        val item = list[position]

        holder.title.text = item.name
        holder.index.text = "Index: ${item.index}"
        holder.tags.text = "Tags: ${item.tags.joinToString(", ")}"
        holder.rating.text = if ((item.rating ?: 0) > 0) "Rating: ${item.rating}" else "Unrated"

        val solvedCount = solvedMap[item.contestId to item.index] ?: 0
        holder.solved.text = "Solved by: $solvedCount users"

        holder.itemView.setOnClickListener {
            val url = "https://codeforces.com/problemset/problem/${item.contestId}/${item.index}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Problem>) {
        list = newList
        notifyDataSetChanged()
    }
}
