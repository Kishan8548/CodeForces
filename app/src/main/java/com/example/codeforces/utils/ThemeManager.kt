package com.example.codeforces.utils

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import com.example.codeforces.R

/**
 * Holds the current user's rank theme across all fragments.
 * Call [apply] once in ProfileFragment after fetching user data.
 * Any fragment/adapter can read the current theme via [current].
 */
object ThemeManager {

    data class RankTheme(
        val rankLabel: String,
        @ColorInt val primary: Int,        // bright accent — avatar ring, badges
        @ColorInt val primaryDim: Int,     // dimmer accent — button hover, icon tint
        @ColorInt val onPrimary: Int,      // text on solid primary background
        @ColorInt val surface: Int,        // card background tint
        @ColorInt val onSurface: Int = Color.parseColor("#DAE6D5"),
        @ColorInt val onSurfaceVariant: Int = Color.parseColor("#B9CCB5"),
        @ColorInt val strokeGray: Int = Color.parseColor("#262626"),
    )

    // Default: Newbie (gray) shown until profile loads
    private val DEFAULT_THEME = RankTheme(
        rankLabel = "Newbie",
        primary = Color.parseColor("#A0A0A0"),
        primaryDim = Color.parseColor("#707070"),
        onPrimary = Color.parseColor("#0D0D0D"),
        surface = Color.parseColor("#181818"),
    )

    var current: RankTheme = DEFAULT_THEME
        private set

    /**
     * Call this from ProfileFragment.updateUI() after user data is loaded.
     */
    fun getThemeForRating(context: Context, rating: Int?): RankTheme {
        val r = rating ?: 0
        return when {
            r < 1200 -> RankTheme(
                rankLabel = "Newbie",
                primary = context.getColor(R.color.rank_newbie_primary),
                primaryDim = context.getColor(R.color.rank_newbie_primary_dim),
                onPrimary = context.getColor(R.color.rank_newbie_on_primary),
                surface = context.getColor(R.color.rank_newbie_surface),
            )
            r < 1400 -> RankTheme(
                rankLabel = "Pupil",
                primary = context.getColor(R.color.rank_pupil_primary),
                primaryDim = context.getColor(R.color.rank_pupil_primary_dim),
                onPrimary = context.getColor(R.color.rank_pupil_on_primary),
                surface = context.getColor(R.color.rank_pupil_surface),
            )
            r < 1600 -> RankTheme(
                rankLabel = "Specialist",
                primary = context.getColor(R.color.rank_specialist_primary),
                primaryDim = context.getColor(R.color.rank_specialist_primary_dim),
                onPrimary = context.getColor(R.color.rank_specialist_on_primary),
                surface = context.getColor(R.color.rank_specialist_surface),
            )
            r < 1900 -> RankTheme(
                rankLabel = "Expert",
                primary = context.getColor(R.color.rank_expert_primary),
                primaryDim = context.getColor(R.color.rank_expert_primary_dim),
                onPrimary = context.getColor(R.color.rank_expert_on_primary),
                surface = context.getColor(R.color.rank_expert_surface),
            )
            r < 2100 -> RankTheme(
                rankLabel = "Candidate Master",
                primary = context.getColor(R.color.rank_cm_primary),
                primaryDim = context.getColor(R.color.rank_cm_primary_dim),
                onPrimary = context.getColor(R.color.rank_cm_on_primary),
                surface = context.getColor(R.color.rank_cm_surface),
            )
            r < 2300 -> RankTheme(
                rankLabel = "Master",
                primary = context.getColor(R.color.rank_master_primary),
                primaryDim = context.getColor(R.color.rank_master_primary_dim),
                onPrimary = context.getColor(R.color.rank_master_on_primary),
                surface = context.getColor(R.color.rank_master_surface),
            )
            r < 2400 -> RankTheme(
                rankLabel = "International Master",
                primary = context.getColor(R.color.rank_im_primary),
                primaryDim = context.getColor(R.color.rank_im_primary_dim),
                onPrimary = context.getColor(R.color.rank_im_on_primary),
                surface = context.getColor(R.color.rank_im_surface),
            )
            r < 2600 -> RankTheme(
                rankLabel = "Grandmaster",
                primary = context.getColor(R.color.rank_gm_primary),
                primaryDim = context.getColor(R.color.rank_gm_primary_dim),
                onPrimary = context.getColor(R.color.rank_gm_on_primary),
                surface = context.getColor(R.color.rank_gm_surface),
            )
            r < 3000 -> RankTheme(
                rankLabel = "International Grandmaster",
                primary = context.getColor(R.color.rank_igm_primary),
                primaryDim = context.getColor(R.color.rank_igm_primary_dim),
                onPrimary = context.getColor(R.color.rank_igm_on_primary),
                surface = context.getColor(R.color.rank_igm_surface),
            )
            else -> RankTheme(
                rankLabel = "Legendary Grandmaster",
                primary = context.getColor(R.color.rank_lgm_primary),
                primaryDim = context.getColor(R.color.rank_lgm_primary_dim),
                onPrimary = context.getColor(R.color.rank_lgm_on_primary),
                surface = context.getColor(R.color.rank_lgm_surface),
            )
        }
    }

    /**
     * Call this from ProfileFragment.updateUI() after user data is loaded.
     */
    fun apply(context: Context, rating: Int?) {
        current = getThemeForRating(context, rating)
    }

    /** Reset to default when user logs out */
    fun reset() {
        current = DEFAULT_THEME
    }
}
