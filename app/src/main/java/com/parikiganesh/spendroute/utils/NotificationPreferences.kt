package com.parikiganesh.spendroute.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.parikiganesh.spendroute.notifications.ReminderScheduler

/**
 * Manages notification preferences for the SpendRoute app.
 * Automatically schedules/cancels daily reminders based on notification state.
 */
class NotificationPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val reminderScheduler = ReminderScheduler(context)

    companion object {
        private const val PREFS_NAME = "spendroute_notifications"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    /**
     * Check if notifications are enabled
     * When enabled, daily reminders are automatically scheduled at 9:00 AM
     * @return true if notifications are enabled, false otherwise (default: false)
     */
    fun isNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }

    /**
     * Set notifications enabled/disabled
     * Automatically schedules reminders when enabled, cancels when disabled
     * @param enabled true to enable notifications, false to disable
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
        }

        // Automatically manage reminders based on notification state
        if (enabled) {
            reminderScheduler.scheduleDailyReminder()
        } else {
            reminderScheduler.cancelDailyReminder()
        }
    }
}





