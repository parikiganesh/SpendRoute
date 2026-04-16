package com.example.spendroute.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.spendroute.data.model.Transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CSV Exporter utility for exporting transactions to CSV file
 * 
 * Format:
 * Date, Time, Category, Amount, Type (Income/Expense), Note
 */
object CsvExporter {
    
    /**
     * Export transactions to CSV file and return the file URI
     * 
     * @param context Android context
     * @param transactions List of transactions to export
     * @return File URI that can be used to share the file
     */
    fun exportTransactionsToCSV(context: Context, transactions: List<Transaction>): Uri? {
        return try {
            // Create filename with timestamp
            val timestamp = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(Date())
            val fileName = "SpendRoute_Export_$timestamp.csv"
            
            // Create file in app's cache directory
            val file = File(context.cacheDir, fileName)
            
            // Create CSV content
            val csvContent = buildCSVContent(transactions)
            
            // Write to file
            file.writeText(csvContent)
            
            // Get URI using FileProvider
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Build CSV content from transactions
     */
    private fun buildCSVContent(transactions: List<Transaction>): String {
        val csvBuilder = StringBuilder()
        
        // Add header
        csvBuilder.append("Date,Time,Category,Amount,Type,Notes\n")
        
        // Add each transaction as a row
        transactions.forEach { transaction ->
            val type = if (transaction.isIncome) "Income" else "Expense"
            val amount = String.format(Locale.US, "%.2f", transaction.amount)
            val noteText = transaction.note?.replace(",", ";") ?: "N/A"  // Replace commas in notes to avoid CSV issues
            
            csvBuilder.append("${transaction.date},${transaction.time},${transaction.category},$amount,$type,$noteText\n")
        }
        
        return csvBuilder.toString()
    }
    
    /**
     * Share CSV file via intent
     * 
     * @param context Android context (Activity context preferred, Application context will be attempted)
     * @param fileUri URI of the CSV file to share
     */
    fun shareCSVFile(context: Context, fileUri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "text/csv"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, "Export Transactions as CSV").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        try {
            // If context is an Activity, use it directly
            if (context is Activity) {
                context.startActivity(chooserIntent)
            } else {
                // For Application context, add FLAG_ACTIVITY_NEW_TASK
                context.startActivity(chooserIntent)
            }
        } catch (e: Exception) {
            println("DEBUG: CSV Share exception: ${e.message}")
            e.printStackTrace()
            throw Exception("Cannot start share activity: ${e.message}")
        }
    }
}


