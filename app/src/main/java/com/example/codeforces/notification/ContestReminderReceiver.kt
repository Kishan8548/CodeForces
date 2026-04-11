package com.example.codeforces

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.app.NotificationCompat
import android.os.Build
import com.example.codeforces.ui.MainActivity

class ContestReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val contestName = intent.getStringExtra("contestName") ?: "Contest"
        val contestId = intent.getIntExtra("contestId", 0)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "contest_reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Contest Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val appIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            contestId,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val contestUrl = if (contestId != 0) {
            "https://codeforces.com/contest/$contestId"
        } else {
            "https://codeforces.com/contests"
        }

        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(contestUrl))
        val webPendingIntent = PendingIntent.getActivity(
            context,
            contestId + 1000,
            webIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.cf_logo
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_reminder)

            .setContentTitle("🚀 Upcoming Contest")
            .setContentText(contestName)

            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Get ready! $contestName is starting very soon.\nTap below to join. 💪")
            )

            .setLargeIcon(largeIcon)

            .setContentIntent(pendingIntent)

            .addAction(
                R.drawable.ic_link,
                "Open Contest",
                webPendingIntent
            )

            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(contestId, notification)
    }
}