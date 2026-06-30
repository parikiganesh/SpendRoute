package com.parikiganesh.spendroute.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility functions for date and time formatting.
 * Provides consistent date/time formatting across the application.
 */
object DateTimeUtils {
    
    /**
     * Get current date as formatted string (e.g., "Apr 10, 2026")
     */
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * Get current time as formatted string (e.g., "2:30 PM")
     */
    fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return timeFormat.format(Date())
    }

    /**
     * Format a date from milliseconds to string (e.g., "Apr 10, 2026")
     */
    fun formatDateFromMillis(millis: Long): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return dateFormat.format(Date(millis))
    }

    /**
     * Format a date from Date object to string (e.g., "Apr 10, 2026")
     */
    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    /**
     * Format time from milliseconds to string (e.g., "2:30 PM")
     */
    fun formatTimeFromMillis(millis: Long): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return timeFormat.format(Date(millis))
    }

    /**
     * Format date-time from milliseconds to string (e.g., "Jun 30, 2026 2:45 PM")
     *
     * Use this for Firestore int64 timestamps like `createdAt`.
     */
    fun formatDateTimeFromMillis(millis: Long): String {
        val dateTimeFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        return dateTimeFormat.format(Date(millis))
    }

    /**
     * Format time from Date object to string (e.g., "2:30 PM")
     */
    fun formatTime(date: Date): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return timeFormat.format(date)
    }

    /**
     * Get current date and time as formatted string
     */
    fun getCurrentDateTime(): String {
        return "${getCurrentDate()} ${getCurrentTime()}"
    }

    /**
     * Convert month name to month number (1-12)
     * e.g., "JANUARY" -> 1, "APRIL" -> 4
     */
    fun getMonthNumber(monthName: String): Int {
        val months = listOf(
            "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
        )
        return months.indexOf(monthName.uppercase()) + 1
    }

    /**
     * Extract month number from date string
     * Supports both full month names and abbreviations
     * e.g., "Apr 10, 2026" -> 4, "April 10, 2026" -> 4
     */
    fun getMonthFromDate(dateString: String): Int {
        return try {
            val monthNames = listOf(
                "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
            )
            val monthAbbr = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )

            // Try to find month abbreviation in the date string
            for ((index, abbr) in monthAbbr.withIndex()) {
                if (dateString.contains(abbr, ignoreCase = true)) {
                    return index + 1
                }
            }

            // Try to find full month name in the date string
            for ((index, fullMonth) in monthNames.withIndex()) {
                if (dateString.contains(fullMonth, ignoreCase = true)) {
                    return index + 1
                }
            }

            0 // No month found
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Extract year from date string
     * Supports "Apr 10, 2026" format
     * e.g., "Apr 10, 2026" -> 2026
     */
    fun getYearFromDate(dateString: String): Int {
        return try {
            // Extract year from date string like "Apr 10, 2026"
            val parts = dateString.split(" ")
            // Year is typically the last part
            parts.lastOrNull()?.toIntOrNull() ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        } catch (e: Exception) {
            java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        }
    }

    /**
     * Convert month number to month name
     * e.g., 1 -> "January", 4 -> "April", etc.
     */
    fun getMonthName(monthNumber: Int): String {
        return when (monthNumber) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> ""
        }
    }

    /**
     * Get current month name in uppercase
     * e.g., "APRIL", "JANUARY", etc.
     */
    fun getCurrentMonthName(): String {
        val calendar = java.util.Calendar.getInstance()
        val monthNumber = calendar.get(java.util.Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
        return getMonthName(monthNumber).uppercase()
    }
}

