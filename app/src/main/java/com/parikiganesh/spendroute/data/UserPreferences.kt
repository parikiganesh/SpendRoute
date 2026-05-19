package com.parikiganesh.spendroute.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "spendroute_prefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_ONBOARDING_COMPLETED = "is_onboarding_completed"
        private const val KEY_ACCOUNT_CREATED_DATE = "account_created_date"
        private const val KEY_HAS_SEEN_GESTURE_HINT = "has_seen_gesture_hint"
    }
    
    // Save username
    fun saveUserName(name: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
        // Also save the account creation date when user name is first set
        if (!isOnboardingCompleted()) {
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
    
    // Mark onboarding as completed
    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_ONBOARDING_COMPLETED, completed).apply()
    }
    
    // Check if onboarding is completed
    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_ONBOARDING_COMPLETED, false)
    }
    
    /**
     * Clear all user data preferences
     * Called when user chooses to clear all data in Profile screen
     */
    fun clearAllUserData() {
        sharedPreferences.edit().apply {
            putString(KEY_USER_NAME, "")
            putBoolean(KEY_IS_ONBOARDING_COMPLETED, false)
            putString(KEY_ACCOUNT_CREATED_DATE, "April 2026")
            putBoolean(KEY_HAS_SEEN_GESTURE_HINT, false)  // Reset gesture hint so it shows again
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
}

