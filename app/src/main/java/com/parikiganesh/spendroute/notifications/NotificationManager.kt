package com.parikiganesh.spendroute.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.parikiganesh.spendroute.MainActivity
import com.parikiganesh.spendroute.R
import com.parikiganesh.spendroute.data.UserPreferences

/**
 * Manages local notifications for the SpendRoute app.
 * Creates and displays daily reminder notifications.
 */
class SpendRouteNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "spendroute_daily_reminder"
        const val CHANNEL_NAME = "Daily Expense Reminders"
        const val NOTIFICATION_ID = 1
        private const val CHANNEL_DESCRIPTION = "Reminds you to log your daily expenses"
    }

    init {
        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    /**
     * Create notification channel for Android 8.0 and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show daily expense reminder notification
     */
    fun showDailyReminderNotification() {
        val userPreferences = UserPreferences(context)
        val userName = userPreferences.getUserName()
        
        val title = if (userName.isNotEmpty()) {
            context.getString(R.string.notification_title_with_name, userName)
        } else {
            context.getString(R.string.notification_title_default)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open MainActivity when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        // Create PendingIntent to handle notification tap
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.notification_content))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notification_big_text))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)  // Add click handler
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle permission denied gracefully
            e.printStackTrace()
        }
    }

    /**
     * Cancel daily reminder notification
     */
    @Suppress("unused")
    fun cancelNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}




