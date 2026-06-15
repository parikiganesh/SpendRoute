package com.parikiganesh.spendroute.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "spendroute_prefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ACCOUNT_CREATED_DATE = "account_created_date"
        private const val KEY_HAS_SEEN_GESTURE_HINT = "has_seen_gesture_hint"
        private const val KEY_INITIAL_CLOUD_MIGRATION_PREFIX = "initial_cloud_migration_"
        private const val KEY_LAST_AUTHENTICATED_UID = "last_authenticated_uid"
    }
    
    // Save username
    fun saveUserName(name: String) {
        val previousName = getUserName()
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
        // Also save the account creation date when user name is first set
        if (previousName.isEmpty() && name.isNotBlank()) {
            saveAccountCreatedDate()
        }
    }
    
    // Save account creation date
    private fun saveAccountCreatedDate() {
        val currentDate = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        sharedPreferences.edit().putString(KEY_ACCOUNT_CREATED_DATE, currentDate).apply()
    }
    
    // Get account creation date
    fun getAccountCreatedDate(): String {
        return sharedPreferences.getString(KEY_ACCOUNT_CREATED_DATE, "April 2026") ?: "April 2026"
    }

    fun setAccountCreatedDate(date: String) {
        sharedPreferences.edit().putString(KEY_ACCOUNT_CREATED_DATE, date).apply()
    }

    // Get user name
    fun getUserName(): String {
        return sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
    }
    
    // Get user initials from name
    fun getUserInitials(): String {
        val name = getUserName()
        if (name.isEmpty()) return ""
        
        val parts = name.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> ""
        }
    }
    
    /**
     * Clear all user data preferences
     * Called when user chooses to clear all data in Profile screen
     */
    fun clearAllUserData() {
        sharedPreferences.edit().apply {
            putString(KEY_USER_NAME, "")
            putString(KEY_ACCOUNT_CREATED_DATE, "April 2026")
            putBoolean(KEY_HAS_SEEN_GESTURE_HINT, false)  // Reset gesture hint so it shows again
            remove(KEY_LAST_AUTHENTICATED_UID)
            apply()
        }
    }

    /**
     * Check if user has seen the gesture hint
     */
    fun hasSeenGestureHint(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_SEEN_GESTURE_HINT, false)
    }

    /**
     * Mark gesture hint as seen or reset it
     */
    fun setGestureHintSeen(seen: Boolean = true) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_GESTURE_HINT, seen).apply()
    }

    fun isInitialCloudMigrationDone(userId: String): Boolean {
        return sharedPreferences.getBoolean("$KEY_INITIAL_CLOUD_MIGRATION_PREFIX$userId", false)
    }

    fun setInitialCloudMigrationDone(userId: String, done: Boolean) {
        sharedPreferences.edit().putBoolean("$KEY_INITIAL_CLOUD_MIGRATION_PREFIX$userId", done).apply()
    }

    fun getLastAuthenticatedUserId(): String? {
        return sharedPreferences.getString(KEY_LAST_AUTHENTICATED_UID, null)
    }

    fun setLastAuthenticatedUserId(userId: String) {
        sharedPreferences.edit().putString(KEY_LAST_AUTHENTICATED_UID, userId).apply()
    }

    fun clearLocalUserProfileCache() {
        sharedPreferences.edit()
            .putString(KEY_USER_NAME, "")
            .putString(KEY_ACCOUNT_CREATED_DATE, "April 2026")
            .apply()
    }
}

