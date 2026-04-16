package com.parikiganesh.spendroute.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility to check and request notification permissions.
 * Handles Android 13+ notification permission requirements.
 */
object PermissionUtils {

    /**
     * Check if notification permission is granted
     * For Android 12 and below, always returns true
     * For Android 13+, checks POST_NOTIFICATIONS permission
     *
     * @param context Application context
     * @return true if notifications are allowed, false otherwise
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 12 and below, notifications are always allowed
            true
        }
    }

    /**
     * Get the required permissions for notifications
     * Returns empty list for Android 12 and below
     * Returns POST_NOTIFICATIONS permission for Android 13+
     *
     * @return Array of permission strings to request
     */
    fun getNotificationPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    }
}

