package com.example.codeforces.adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.example.codeforces.databinding.ProblemItemBinding
import com.example.codeforces.models.Problem
import com.example.codeforces.models.ProblemStatistics
import com.example.codeforces.utils.ThemeManager

class ProblemsAdapter(
    private var list: List<Problem>,
    private var statistics: List<ProblemStatistics>
) : RecyclerView.Adapter<ProblemsAdapter.ProblemViewHolder>() {

    private var solvedMap: Map<Pair<Int?, String?>, Int> =
        statistics.associateBy({ it.contestId to it.index }, { it.solvedCount })

    inner class ProblemViewHolder(val binding: ProblemItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemViewHolder {
        val binding = ProblemItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProblemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        val item = list[position]
        val theme = ThemeManager.current

        holder.binding.apply {
            textTitle.text = item.name

            textIndex.text = item.index ?: "—"
            textIndex.backgroundTintList = ColorStateList.valueOf(theme.primary)
            textIndex.setTextColor(theme.onPrimary)

            val ratingStr = if ((item.rating ?: 0) > 0) "★ ${item.rating}" else "★ —"
            textRating.text = ratingStr
            textRating.setTextColor(theme.primary)

            (cardProblemRoot.background?.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(theme.surface)

            textTags.text = item.tags.joinToString(" · ").ifBlank { "—" }

            val solvedCount = solvedMap[item.contestId to item.index] ?: 0
            textSolved.text = "Solved: $solvedCount users"

            problemAccentBar.setBackgroundColor(theme.primaryDim)

            root.setOnClickListener {
                val url = "https://codeforces.com/problemset/problem/${item.contestId}/${item.index}"
                try {
                    CustomTabsIntent.Builder().build().launchUrl(root.context, Uri.parse(url))
                } catch (e: Exception) {
                    root.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Problem>, newStatistics: List<ProblemStatistics>? = null) {
        list = newList
        newStatistics?.let {
            statistics = it
            solvedMap = statistics.associateBy({ it.contestId to it.index }, { it.solvedCount })
        }
        notifyDataSetChanged()
    }
}
