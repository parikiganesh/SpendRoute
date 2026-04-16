package com.example.spendroute.notifications

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Schedules and manages daily expense reminder notifications.
 * Automatically handles WorkManager setup and cancellation.
 */
class ReminderScheduler(private val context: Context) {

    companion object {
        private const val MORNING_REMINDER_WORK_NAME = "morning_expense_reminder"
        private const val EVENING_REMINDER_WORK_NAME = "evening_expense_reminder"
        
        // Morning reminder at 9 AM
        private const val MORNING_HOUR = 9
        private const val MORNING_MINUTE = 0
        
        // Evening reminder at 6 PM
        private const val EVENING_HOUR = 18
        private const val EVENING_MINUTE = 0
    }

    /**
     * Schedule daily reminder notifications
     * Triggers daily at 9:00 AM and 6:00 PM
     */
    fun scheduleDailyReminder() {
        // Schedule morning reminder at 9 AM
        scheduleMorningReminder()
        
        // Schedule evening reminder at 6 PM
        scheduleEveningReminder()
    }
    
    /**
     * Schedule morning reminder at 9 AM
     */
    private fun scheduleMorningReminder() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, MORNING_HOUR)
            set(Calendar.MINUTE, MORNING_MINUTE)
            set(Calendar.SECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()
        val initialDelay = TimeUnit.MILLISECONDS.toMinutes(delay)

        val morningReminderWork = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1,
            TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .addTag(MORNING_REMINDER_WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MORNING_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            morningReminderWork
        )
    }
    
    /**
     * Schedule evening reminder at 6 PM
     */
    private fun scheduleEveningReminder() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, EVENING_HOUR)
            set(Calendar.MINUTE, EVENING_MINUTE)
            set(Calendar.SECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()
        val initialDelay = TimeUnit.MILLISECONDS.toMinutes(delay)

        val eveningReminderWork = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1,
            TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .addTag(EVENING_REMINDER_WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EVENING_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            eveningReminderWork
        )
    }

    /**
     * Cancel daily reminder notifications
     */
    fun cancelDailyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(MORNING_REMINDER_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(EVENING_REMINDER_WORK_NAME)
    }

    /**
     * Check if daily reminders are scheduled
     * @return true if reminders are scheduled, false otherwise
     */
    @Suppress("unused")
    fun isDailyReminderScheduled(): Boolean {
        val morningWorkInfos = WorkManager.getInstance(context)
            .getWorkInfosByTag(MORNING_REMINDER_WORK_NAME)
        val eveningWorkInfos = WorkManager.getInstance(context)
            .getWorkInfosByTag(EVENING_REMINDER_WORK_NAME)
        
        return try {
            val morningList = morningWorkInfos.get()
            val eveningList = eveningWorkInfos.get()
            
            val morningScheduled = morningList.isNotEmpty() && !morningList[0].state.isFinished
            val eveningScheduled = eveningList.isNotEmpty() && !eveningList[0].state.isFinished
            
            morningScheduled && eveningScheduled
        } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
            false
        }
    }
}


