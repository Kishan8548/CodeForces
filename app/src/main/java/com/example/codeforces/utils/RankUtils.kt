package com.example.codeforces.utils

import android.content.Context
import com.example.codeforces.R

object RankUtils {

    /**
     * Returns a Pair of (rankTitle, colorInt) for a given Codeforces rating.
     * Usable from both Activities/Fragments and widget RemoteViews.
     */
    fun getRankAndColor(context: Context, rating: Int?): Pair<String, Int> {
        val r = rating ?: 0
        return when {
            r < 1200 -> "Newbie" to context.getColor(R.color.newbie)
            r < 1400 -> "Pupil" to context.getColor(R.color.pupil)
            r < 1600 -> "Specialist" to context.getColor(R.color.specialist)
            r < 1900 -> "Expert" to context.getColor(R.color.expert)
            r < 2100 -> "Candidate Master" to context.getColor(R.color.candidate_master)
            r < 2300 -> "Master" to context.getColor(R.color.master)
            r < 2400 -> "International Master" to context.getColor(R.color.international_master)
            r < 2600 -> "Grandmaster" to context.getColor(R.color.grandmaster)
            r < 3000 -> "International Grandmaster" to context.getColor(R.color.international_grandmaster)
            else -> "Legendary Grandmaster" to context.getColor(R.color.legendary_grandmaster)
        }
    }

    /**
     * Returns just the hex color int for a rating (useful for RemoteViews.setTextColor).
     */
    fun getRankColor(context: Context, rating: Int?): Int {
        return getRankAndColor(context, rating).second
    }
}
