package com.example.spendroute.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Worker class for scheduling daily expense reminder notifications.
 * Uses WorkManager to trigger notifications at a specific time daily.
 */
class DailyReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            // Show the daily reminder notification
            val notificationManager = SpendRouteNotificationManager(applicationContext)
            notificationManager.showDailyReminderNotification()
            
            // Return success to indicate the work completed successfully
            Result.success()
        } catch (e: Exception) {
            // Log the error and retry
            e.printStackTrace()
            Result.retry()
        }
    }
}

