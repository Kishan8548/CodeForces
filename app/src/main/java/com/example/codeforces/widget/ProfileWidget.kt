package com.example.codeforces.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.codeforces.R
import com.example.codeforces.api.RetrofitInstance
import com.example.codeforces.ui.EnterUsernameActivity
import com.example.codeforces.ui.MainActivity
import com.example.codeforces.utils.RankUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ProfileWidget : AppWidgetProvider() {

    companion object {
        private const val TAG = "ProfileWidget"
        const val ACTION_REFRESH = "com.example.codeforces.widget.ACTION_REFRESH"

        /**
         * Triggers an update for ALL placed widget instances.
         * Call this after login/logout to keep widgets in sync.
         */
        fun triggerUpdate(context: Context) {
            val intent = Intent(context, ProfileWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val widgetManager = AppWidgetManager.getInstance(context)
            val ids = widgetManager.getAppWidgetIds(
                ComponentName(context, ProfileWidget::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }

    /**
     * Override onReceive to handle our custom refresh action and,
     * critically, to use goAsync() so the process stays alive
     * while the coroutine fetches data from the network.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH) {
            // Manual refresh: treat it like a regular update
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, ProfileWidget::class.java)
            )
            onUpdate(context, appWidgetManager, ids)
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = context.getSharedPreferences("CodeforcesPrefs", Context.MODE_PRIVATE)
        val handle = prefs.getString("HANDLE", null)

        val views = RemoteViews(context.packageName, R.layout.widget_profile)

        // === Set up the refresh button PendingIntent ===
        val refreshIntent = Intent(context, ProfileWidget::class.java).apply {
            action = ACTION_REFRESH
        }
        val refreshPending = PendingIntent.getBroadcast(
            context, 1, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPending)

        // === Handle not-logged-in state ===
        if (handle.isNullOrBlank()) {
            views.setTextViewText(R.id.widget_handle, context.getString(R.string.widget_not_logged_in))
            views.setTextViewText(R.id.widget_rank, context.getString(R.string.widget_tap_to_login))
            views.setTextViewText(R.id.widget_rating, "–")
            views.setTextViewText(R.id.widget_max_rating, "–")

            // Tap opens the login screen
            val loginIntent = Intent(context, EnterUsernameActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val loginPending = PendingIntent.getActivity(
                context, 0, loginIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, loginPending)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }

        // === Set up tap-to-open: opens MainActivity on the Profile tab ===
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "profile")
        }
        val openPending = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openPending)

        // Show loading state
        views.setTextViewText(R.id.widget_handle, handle)
        views.setTextViewText(R.id.widget_rank, "")
        views.setTextViewText(R.id.widget_rating, "…")
        views.setTextViewText(R.id.widget_max_rating, "…")
        appWidgetManager.updateAppWidget(appWidgetId, views)

        // === Fetch data using goAsync() so the process stays alive ===
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.getUserInfo(handle)
                val user = response.body()?.result?.firstOrNull()

                val submissionResponse = RetrofitInstance.api.getUserSubmissions(handle)
                val submissions = submissionResponse.body()?.result?: emptyList()

                var todayCount = 0

                for (submission in submissions){
                    if (isToday(submission.creationTimeSeconds)){
                        todayCount++;
                    }
                }

                if (todayCount>0){
                    views.setTextViewText(R.id.widget_todays_submission,todayCount.toString())
                    views.setViewVisibility(R.id.checkSubmission, View.VISIBLE)
                }
                else{
                    views.setTextViewText(R.id.widget_todays_submission,"0")
                    views.setViewVisibility(R.id.checkSubmission, View.GONE)
                }

                if (user != null) {
                    views.setTextViewText(R.id.widget_handle, user.handle)
                    views.setTextColor(R.id.widget_handle, RankUtils.getRankColor(context, user.rating))

                    // Rating
                    val currentRating = user.rating ?: 0
                    views.setTextViewText(R.id.widget_rating, currentRating.toString())
                    views.setTextColor(R.id.widget_rating, RankUtils.getRankColor(context, user.rating))

                    // Max rating
                    val maxRating = user.maxRating ?: 0
                    views.setTextViewText(R.id.widget_max_rating, maxRating.toString())
                    views.setTextColor(R.id.widget_max_rating, RankUtils.getRankColor(context, user.maxRating))

                    // Rank badge
                    val rankText = user.rank ?: context.getString(R.string.widget_unrated)
                    views.setTextViewText(R.id.widget_rank, rankText)
                    views.setTextColor(R.id.widget_rank, RankUtils.getRankColor(context, user.rating))
                    
                    // Dynamic Widget Theme
                    val theme = com.example.codeforces.utils.ThemeManager.getThemeForRating(context, user.rating)
                    views.setInt(R.id.widget_border, "setBackgroundColor", theme.strokeGray)
                    views.setInt(R.id.widget_root, "setBackgroundColor", theme.surface)
                    views.setInt(R.id.widget_divider_1, "setBackgroundColor", theme.strokeGray)
                    views.setInt(R.id.widget_divider_2, "setBackgroundColor", theme.strokeGray)
                } else {
                    views.setTextViewText(R.id.widget_rating, "–")
                    views.setTextViewText(R.id.widget_max_rating, "–")
                    views.setTextViewText(R.id.widget_rank, context.getString(R.string.widget_error))
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch profile: ${e.message}", e)
                views.setTextViewText(R.id.widget_rating, "–")
                views.setTextViewText(R.id.widget_max_rating, "–")
                views.setTextViewText(R.id.widget_rank, context.getString(R.string.widget_error))
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } finally {
                // CRITICAL: Signal that the BroadcastReceiver work is done.
                // Without this, Android may kill the process before the network call completes,
                // which was the root cause of the "Failed to load" bug.
                pendingResult.finish()
            }
        }
    }

    private fun isToday(creationTimeSeconds : Long) : Boolean{
        val submissionCal = Calendar.getInstance()
        submissionCal.timeInMillis = creationTimeSeconds * 1000

        val todayCal = Calendar.getInstance()

        return submissionCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) && submissionCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)

    }

}