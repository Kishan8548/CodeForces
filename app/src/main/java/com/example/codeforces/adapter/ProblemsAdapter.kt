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

class ProblemsAdapter(private var list: List<Problem>) :
    RecyclerView.Adapter<ProblemsAdapter.ProblemViewHolder>() {

    inner class ProblemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textTitle)
        val tags: TextView = view.findViewById(R.id.textTags)
        val rating: TextView = view.findViewById(R.id.textRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.problem_item, parent, false)
        return ProblemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.name
        holder.tags.text = item.tags.joinToString(", ")
        item.rating?.let { holder.rating.text = if (it > 0) "Rating: ${item.rating}" else "Unrated" }

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
