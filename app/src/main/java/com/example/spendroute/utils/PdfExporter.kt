package com.example.spendroute.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.spendroute.data.model.Transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PDF Exporter utility for creating formatted PDF reports of transactions
 * 
 * Features:
 * - Summary statistics (Total Income, Expense, Balance)
 * - Formatted transaction list
 * - Professional layout with title and timestamp
 */
object PdfExporter {
    
    private const val PAGE_WIDTH = 595  // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 40
    private const val LINE_HEIGHT = 20
    
    /**
     * Export transactions to PDF file and return the file URI
     * 
     * @param context Android context
     * @param transactions List of transactions to export
     * @return File URI that can be used to share the file
     */
    fun exportTransactionsToPDF(context: Context, transactions: List<Transaction>): Uri? {
        return try {
            // Create filename with timestamp
            val timestamp = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(Date())
            val fileName = "SpendRoute_Report_$timestamp.pdf"
            
            // Create file in app's cache directory
            val file = File(context.cacheDir, fileName)
            
            // Create PDF document
            val pdfDocument = PdfDocument()
            
            // Calculate totals
            val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
            val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
            val totalBalance = totalIncome - totalExpense
            
            // Create pages
            var pageNumber = 1
            var yPosition = MARGIN
            var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            var canvas = page!!.canvas
            
            // Draw header
            yPosition = drawHeader(canvas, yPosition)
            
            // Draw summary
            yPosition = drawSummary(canvas, yPosition, totalIncome, totalExpense, totalBalance)
            
            // Draw transactions
            for (transaction in transactions) {
                // Check if we need a new page
                if (yPosition + LINE_HEIGHT * 3 > PAGE_HEIGHT - MARGIN) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                    canvas = page!!.canvas
                    yPosition = MARGIN
                }
                
                yPosition = drawTransaction(canvas, yPosition, transaction)
            }
            
            // Finish last page
            pdfDocument.finishPage(page)
            
            // Save PDF to file
            pdfDocument.writeTo(file.outputStream())
            pdfDocument.close()
            
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
     * Draw PDF header
     */
    private fun drawHeader(canvas: Canvas, startY: Int): Int {
        val titlePaint = Paint().apply {
            textSize = 24f
            isAntiAlias = true
            color = 0xFF5B4B9B.toInt()
        }
        
        val subtitlePaint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
            color = 0xFF9E9E9E.toInt()
        }
        
        canvas.drawText("SpendRoute Expense Report", MARGIN.toFloat(), startY.toFloat(), titlePaint)
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateText = "Generated: ${dateFormat.format(Date())}"
        canvas.drawText(dateText, MARGIN.toFloat(), (startY + LINE_HEIGHT).toFloat(), subtitlePaint)
        
        return startY + LINE_HEIGHT * 3
    }
    
    /**
     * Draw summary statistics
     */
    private fun drawSummary(
        canvas: Canvas,
        startY: Int,
        totalIncome: Double,
        totalExpense: Double,
        totalBalance: Double
    ): Int {
        val titlePaint = Paint().apply {
            textSize = 14f
            isAntiAlias = true
            color = 0xFF1C1B1F.toInt()
        }
        
        val valuePaint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
            color = 0xFF5B4B9B.toInt()
        }
        
        val incomeColor = Paint().apply {
            textSize = 12f
            isAntiAlias = true
            color = 0xFF4CAF50.toInt()
        }
        
        val expenseColor = Paint().apply {
            textSize = 12f
            isAntiAlias = true
            color = 0xFFE53935.toInt()
        }
        
        var yPosition = startY
        
        canvas.drawText("Summary", MARGIN.toFloat(), yPosition.toFloat(), titlePaint)
        yPosition += LINE_HEIGHT + 5
        
        canvas.drawText("Total Income:", MARGIN.toFloat(), yPosition.toFloat(), titlePaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", totalIncome)}", 200f, yPosition.toFloat(), incomeColor)
        yPosition += LINE_HEIGHT
        
        canvas.drawText("Total Expense:", MARGIN.toFloat(), yPosition.toFloat(), titlePaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", totalExpense)}", 200f, yPosition.toFloat(), expenseColor)
        yPosition += LINE_HEIGHT
        
        canvas.drawText("Balance:", MARGIN.toFloat(), yPosition.toFloat(), titlePaint)
        canvas.drawText("₹${String.format(Locale.US, "%.2f", totalBalance)}", 200f, yPosition.toFloat(), valuePaint)
        
        return yPosition + LINE_HEIGHT * 3
    }
    
    /**
     * Draw a single transaction
     */
    private fun drawTransaction(canvas: Canvas, startY: Int, transaction: Transaction): Int {
        val labelPaint = Paint().apply {
            textSize = 11f
            isAntiAlias = true
            color = 0xFF1C1B1F.toInt()
        }
        
        val valuePaint = Paint().apply {
            textSize = 11f
            isAntiAlias = true
            color = 0xFF9E9E9E.toInt()
        }
        
        val amountColor = Paint().apply {
            textSize = 11f
            isAntiAlias = true
            color = if (transaction.isIncome) 0xFF4CAF50.toInt() else 0xFFE53935.toInt()
        }
        
        var yPosition = startY
        
        // Transaction title and category
        canvas.drawText("${transaction.title} • ${transaction.category}", MARGIN.toFloat(), yPosition.toFloat(), labelPaint)
        yPosition += LINE_HEIGHT
        
        // Date, Time, and Amount
        val amountText = if (transaction.isIncome) "+₹${transaction.amount.toInt()}" else "-₹${transaction.amount.toInt()}"
        canvas.drawText("${transaction.date} • ${transaction.time}", MARGIN.toFloat(), yPosition.toFloat(), valuePaint)
        canvas.drawText(amountText, 400f, yPosition.toFloat(), amountColor)
        yPosition += LINE_HEIGHT
        
        // Note if available
        if (!transaction.note.isNullOrEmpty()) {
            canvas.drawText("Note: ${transaction.note}", MARGIN.toFloat(), yPosition.toFloat(), valuePaint)
            yPosition += LINE_HEIGHT
        }
        
        return yPosition
    }
    
    /**
     * Share PDF file via intent
     * 
     * @param context Android context (Activity context preferred, Application context will be attempted)
     * @param fileUri URI of the PDF file to share
     */
    fun sharePDFFile(context: Context, fileUri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "application/pdf"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, "Export Transactions as PDF").apply {
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
            println("DEBUG: PDF Share exception: ${e.message}")
            e.printStackTrace()
            throw Exception("Cannot start share activity: ${e.message}")
        }
    }
}


