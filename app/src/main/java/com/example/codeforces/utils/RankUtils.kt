package com.example.codeforces.utils

import android.content.Context
import androidx.annotation.ColorInt
import com.example.codeforces.R

object RankUtils {

    /**
     * Returns (rankLabel, colorInt) for use where just a simple color is needed
     * (widget RemoteViews, legacy TextViews, etc.).
     */
    fun getRankAndColor(context: Context, rating: Int?): Pair<String, Int> {
        val r = rating ?: 0
        return when {
            r < 1200 -> "Newbie"           to context.getColor(R.color.rank_newbie_primary)
            r < 1400 -> "Pupil"            to context.getColor(R.color.rank_pupil_primary)
            r < 1600 -> "Specialist"       to context.getColor(R.color.rank_specialist_primary)
            r < 1900 -> "Expert"           to context.getColor(R.color.rank_expert_primary)
            r < 2100 -> "Candidate Master" to context.getColor(R.color.rank_cm_primary)
            r < 2300 -> "Master"           to context.getColor(R.color.rank_master_primary)
            r < 2400 -> "International Master" to context.getColor(R.color.rank_im_primary)
            r < 2600 -> "Grandmaster"      to context.getColor(R.color.rank_gm_primary)
            r < 3000 -> "International Grandmaster" to context.getColor(R.color.rank_igm_primary)
            else     -> "Legendary Grandmaster"    to context.getColor(R.color.rank_lgm_primary)
        }
    }

    /** Returns just the accent color Int for a rating. */
    @ColorInt
    fun getRankColor(context: Context, rating: Int?): Int =
        getRankAndColor(context, rating).second

    /**
     * Apply the full theme to ThemeManager AND return (label, color).
     * Call this in ProfileFragment.updateUI() to update the global theme.
     */
    fun applyTheme(context: Context, rating: Int?): Pair<String, Int> {
        ThemeManager.apply(context, rating)
        return getRankAndColor(context, rating)
    }

    /**
     * Returns just the rank label for any rating — no Context needed.
     * Used in adapters where you only need the label string.
     */
    fun getRankInfo(rating: Int?): Pair<String, Int> {
        val r = rating ?: 0
        val label = when {
            r < 1200 -> "Newbie"
            r < 1400 -> "Pupil"
            r < 1600 -> "Specialist"
            r < 1900 -> "Expert"
            r < 2100 -> "Candidate Master"
            r < 2300 -> "Master"
            r < 2400 -> "International Master"
            r < 2600 -> "Grandmaster"
            r < 3000 -> "International Grandmaster"
            else     -> "Legendary Grandmaster"
        }
        return label to r
    }
}
