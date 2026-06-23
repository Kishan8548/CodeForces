package com.example.codeforces.adapter

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.codeforces.ContestReminderReceiver
import com.example.codeforces.databinding.ItemContestBinding
import com.example.codeforces.models.Contest
import com.example.codeforces.utils.ThemeManager
import java.util.concurrent.TimeUnit

class ContestAdapter : RecyclerView.Adapter<ContestAdapter.ViewHolder>() {

    private val contestList = mutableListOf<Contest>()

    inner class ViewHolder(val binding: ItemContestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contest = contestList[position]
        val theme = ThemeManager.current
        val ctx = holder.binding.root.context

        holder.binding.apply {

            // Contest name
            textContestName.text = contest.name

            // Type badge — extract Div from name or use contest type
            val divText = when {
                contest.name.contains("Div. 1", true) && contest.name.contains("Div. 2", true) -> "DIV 1+2"
                contest.name.contains("Div. 1", true) -> "DIV 1"
                contest.name.contains("Div. 2", true) -> "DIV 2"
                contest.name.contains("Div. 3", true) -> "DIV 3"
                contest.name.contains("Div. 4", true) -> "DIV 4"
                contest.name.contains("Educational", true) -> "EDU"
                contest.name.contains("Global", true) -> "GLOBAL"
                else -> contest.type ?: "CF"
            }
            textContestType.text = divText
            textContestType.backgroundTintList = ColorStateList.valueOf(theme.primary)
            textContestType.setTextColor(theme.onPrimary)

            (cardContestRoot.background?.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(theme.surface)
            btnRegister.backgroundTintList = ColorStateList.valueOf(theme.primary)
            btnRegister.setTextColor(theme.onPrimary)

            // Phase badge
            textPhase.text = when (contest.phase) {
                "BEFORE" -> "UPCOMING"
                "CODING" -> "RUNNING"
                "FINISHED" -> "FINISHED"
                else -> contest.phase ?: "—"
            }
            textPhase.setTextColor(when (contest.phase) {
                "CODING" -> theme.primary
                "FINISHED" -> ctx.getColor(com.example.codeforces.R.color.on_surface_variant)
                else -> ctx.getColor(com.example.codeforces.R.color.on_surface_variant)
            })

            // LIVE NOW indicator
            textLiveNow.visibility = if (contest.phase == "CODING") View.VISIBLE else View.GONE
            textLiveNow.setTextColor(theme.primary)

            // Duration
            val hours = (contest.durationSeconds ?: 0) / 3600
            val mins = ((contest.durationSeconds ?: 0) % 3600) / 60
            textDuration.text = if (mins > 0) "${hours}h ${mins}m" else "${hours}h"

            // Timer label
            tvTimerLabel.text = when (contest.phase) {
                "BEFORE" -> "STARTS IN"
                "CODING" -> "TIME LEFT"
                else -> "ENDED"
            }

            // Countdown timer
            if (contest.phase == "BEFORE" || contest.phase == "CODING") {
                val currentTime = System.currentTimeMillis() / 1000
                val target = if (contest.phase == "BEFORE") {
                    contest.startTimeSeconds?.minus(currentTime)
                } else {
                    val end = (contest.startTimeSeconds ?: 0) + (contest.durationSeconds ?: 0)
                    end - currentTime
                }
                if (target != null && target > 0) {
                    object : CountDownTimer(target * 1000, 1000) {
                        override fun onTick(ms: Long) {
                            val h = TimeUnit.MILLISECONDS.toHours(ms)
                            val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
                            val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
                            textTimer.text = String.format("%02d:%02d:%02d", h, m, s)
                        }
                        override fun onFinish() { textTimer.text = "NOW" }
                    }.start()
                } else {
                    textTimer.text = if (contest.phase == "CODING") "LIVE" else "NOW"
                }
                textTimer.setTextColor(
                    if (contest.phase == "CODING") theme.primary
                    else ctx.getColor(com.example.codeforces.R.color.danger_red)
                )
            } else {
                textTimer.text = "——"
                textTimer.setTextColor(ctx.getColor(com.example.codeforces.R.color.on_surface_variant))
            }

            // ── Register / Enter Arena / Standings ──────────────────────────
            val btnLabel = when (contest.phase) {
                "CODING" -> "ENTER ARENA"
                "FINISHED" -> "STANDINGS"
                else -> "REGISTER"
            }
            btnRegister.text = btnLabel
            btnRegister.backgroundTintList = ColorStateList.valueOf(
                if (contest.phase == "BEFORE") theme.primary else theme.primaryDim
            )
            btnRegister.setTextColor(theme.onPrimary)
            btnRegister.setOnClickListener {
                val url = when (contest.phase) {
                    "BEFORE" -> "https://codeforces.com/contestRegistration/${contest.id}"
                    "FINISHED" -> "https://codeforces.com/contest/${contest.id}/standings"
                    else -> "https://codeforces.com/contest/${contest.id}"
                }
                openInBrowser(ctx, url)
            }

            // ── Reminder button ──────────────────────────────────────────────
            // Only show for upcoming or ongoing contests
            if (contest.phase == "FINISHED") {
                btnSetReminder.isEnabled = false
                btnSetReminder.alpha = 0.35f
                btnSetReminder.text = "REMIND"
            } else {
                btnSetReminder.isEnabled = true
                btnSetReminder.alpha = 1f
                btnSetReminder.text = "REMIND"
                btnSetReminder.setTextColor(theme.primary)
            }
            btnSetReminder.setOnClickListener {
                showReminderDialog(ctx, contest)
            }
        }
    }

    private fun openInBrowser(context: Context, url: String) {
        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    fun showReminderDialog(context: Context, contest: Contest) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 10)
        }
        val input = EditText(context).apply {
            hint = "Time before contest"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val spinner = Spinner(context)
        spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Minutes", "Hours", "Days"))
        layout.addView(input)
        layout.addView(spinner)

        MaterialAlertDialogBuilder(context)
            .setTitle("Set Reminder for\n${contest.name}")
            .setView(layout)
            .setPositiveButton("Set") { _, _ ->
                val value = input.text.toString().toLongOrNull()
                if (value == null || value <= 0) {
                    Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val offset = when (spinner.selectedItem.toString()) {
                    "Minutes" -> value * 60 * 1000
                    "Hours" -> value * 60 * 60 * 1000
                    "Days" -> value * 24 * 60 * 60 * 1000
                    else -> 0L
                }
                scheduleContestReminder(context, contest, offset)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @SuppressLint("ServiceCast")
    fun scheduleContestReminder(context: Context, contest: Contest, offset: Long) {
        val intent = Intent(context, ContestReminderReceiver::class.java).apply {
            putExtra("contest_name", contest.name)
            putExtra("contest_url", "https://codeforces.com/contest/${contest.id}")
        }
        val pendingIntent = PendingIntent.getBroadcast(context, contest.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = contest.startTimeSeconds?.times(1000)?.minus(offset)
        if (triggerAt != null) {
            if (triggerAt > System.currentTimeMillis()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                Toast.makeText(context, "✓ Reminder set for ${contest.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Contest already started or finished", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = contestList.size

    fun submitList(list: List<Contest>) {
        contestList.clear()
        contestList.addAll(list)
        notifyDataSetChanged()
    }
}
