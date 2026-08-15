package com.caloriecalc.app.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.caloriecalc.app.MainActivity

object NotificationHelper {

    const val WEIGHT_REMINDER_CHANNEL_ID = "weight_reminder"
    private const val WEIGHT_REMINDER_NOTIFICATION_ID = 1001
    private const val WEIGHT_FOLLOW_UP_NOTIFICATION_ID = 1002

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WEIGHT_REMINDER_CHANNEL_ID,
                "Weight reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminder to log your body weight"
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    fun showWeightReminder(context: Context) {
        notifyWeight(
            context = context,
            notificationId = WEIGHT_REMINDER_NOTIFICATION_ID,
            title = "Log today's weight",
            body = "Keep your weight trend accurate — it only takes a few seconds.",
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    /** Second nudge, hours after the first went unanswered — deliberately higher priority so
     * it isn't silently buried alongside the one that was already ignored. */
    fun showWeightFollowUpReminder(context: Context) {
        notifyWeight(
            context = context,
            notificationId = WEIGHT_FOLLOW_UP_NOTIFICATION_ID,
            title = "Still no weight logged today",
            body = "A gap in the trend makes the calorie adjustment less accurate. Log it now?",
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    private fun notifyWeight(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
        priority: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, WEIGHT_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
