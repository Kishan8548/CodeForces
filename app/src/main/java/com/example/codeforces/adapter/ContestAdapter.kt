package com.example.codeforces.ui

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.codeforces.ContestReminderReceiver
import com.example.codeforces.databinding.ItemContestBinding
import com.example.codeforces.models.Contest
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

        holder.binding.apply {
            textContestName.text = contest.name
            textContestType.text = "Type: ${contest.type}"
            textPhase.text = "Phase: ${contest.phase}"
            textDuration.text = "Duration: ${contest.durationSeconds / 3600} hrs"

            // Timer logic
            if (contest.phase == "BEFORE") {
                val currentTime = System.currentTimeMillis() / 1000
                val remaining = contest.startTimeSeconds?.minus(currentTime)
                if (remaining != null) {
                    if (remaining > 0) {
                        object : CountDownTimer(remaining * 1000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                val h = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                                val m = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                                val s = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                                textTimer.text = String.format("%02d:%02d:%02d", h, m, s)
                            }

                            override fun onFinish() {
                                textTimer.text = "Started"
                            }
                        }.start()
                    } else {
                        textTimer.text = "Started"
                    }
                }
            } else {
                textTimer.text = "Running / Finished"
            }

            btnOpenContest.setOnClickListener {
                val context = root.context
                val url = if (contest.phase == "BEFORE") {
                    "https://codeforces.com/contests"
                } else {
                    "https://codeforces.com/contest/${contest.id}"
                }

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
            btnSetReminder.setOnClickListener {
                scheduleContestReminder(root.context, contest)
            }



        }
    }
    @SuppressLint("ServiceCast")
    fun scheduleContestReminder(context: Context, contest: Contest) {
        val intent = Intent(context, ContestReminderReceiver::class.java).apply {
            putExtra("contest_name", contest.name)
            putExtra("contest_url", "https://codeforces.com/contest/${contest.id}")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            contest.id, // unique request code per contest
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerAt = contest.startTimeSeconds?.times(1000)?.minus(10 * 60 * 1000)
        if (triggerAt != null) {
            if (triggerAt > System.currentTimeMillis()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                Toast.makeText(context, "Reminder set for ${contest.name}", Toast.LENGTH_SHORT).show()
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
