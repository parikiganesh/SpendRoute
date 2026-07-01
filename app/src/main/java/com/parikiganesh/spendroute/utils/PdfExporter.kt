package com.parikiganesh.spendroute.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.parikiganesh.spendroute.data.model.Transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * PDF Exporter utility for creating formatted PDF reports of transactions
 * 
 * Features:
 * - Summary statistics (Total Income, Expense, Balance)
 * - Formatted transaction list
 * - Professional layout with title and timestamp
 */
object PdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40
    private const val SIDE_MARGIN = 24f
    private const val FOOTER_SPACE = 60
    private const val HEADER_GAP = 18
    private const val TABLE_ROW_HEIGHT = 42f
    private const val TABLE_HEADER_HEIGHT = 34f
    private const val TOTAL_ROW_HEIGHT = 48f

    private val borderColor = 0xFFD9DCE3.toInt()
    private val primaryColor = 0xFF4A3FB0.toInt()
    private val subtleTextColor = 0xFF6E7485.toInt()

    private data class StatementRow(
        val dateTime: String,
        val description: String,
        val category: String,
        val type: String,
        val amount: Double,
        val balance: Double,
        val isIncome: Boolean
    )

    private data class TableColumn(val title: String, val width: Float)

    private val baseTableColumns = listOf(
        TableColumn("Date", 92f),
        TableColumn("Description", 108f),
        TableColumn("Category", 82f),
        TableColumn("Type", 58f),
        TableColumn("Amount (Rs)", 82f)
    )

    private val tableColumns: List<TableColumn>
        get() {
            val totalTableWidth = PAGE_WIDTH - (SIDE_MARGIN * 2f)
            val fixedWidth = baseTableColumns.sumOf { it.width.toDouble() }.toFloat()
            val balanceWidth = (totalTableWidth - fixedWidth).coerceAtLeast(80f)
            return baseTableColumns + TableColumn("Balance (Rs)", balanceWidth)
        }

    private val dateTimeParsers = listOf(
        SimpleDateFormat("MMM d, yyyy h:mm a", Locale.ENGLISH),
        SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
    )

    /**
     * Export transactions to PDF file and return the file URI
     * 
     * @param context Android context
     * @param transactions List of transactions to export
     * @param userName User name shown in the header area (optional)
     * @return File URI that can be used to share the file
     */
    fun exportTransactionsToPDF(context: Context, transactions: List<Transaction>, userName: String = ""): Uri? {
        val timestamp = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "SpendRoute_Report_$timestamp.pdf")
        val sortedTransactions = transactions.sortedBy { parseTransactionTime(it) }
        val totalIncome = sortedTransactions.filter { it.isIncome }.sumOf { it.amount }
        val totalExpense = sortedTransactions.filter { !it.isIncome }.sumOf { it.amount }
        val closingBalance = totalIncome - totalExpense
        val reportPeriod = buildReportPeriod(sortedTransactions)
        val generatedOn = SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault()).format(Date())
        val rows = buildRowsWithRunningBalance(sortedTransactions)

        val pdfDocument = PdfDocument()
        return try {
            var pageNumber = 1
            var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            var canvas = page.canvas
            var y = MARGIN.toFloat()

            y = drawReportHeader(canvas, y, reportPeriod, generatedOn)
            y = drawNameRow(canvas, y, userName)
            y += HEADER_GAP
            y = drawSummaryCards(canvas, y, totalIncome, totalExpense, closingBalance, rows.size)
            y += HEADER_GAP
            y = drawSectionTitle(canvas, y, "TRANSACTIONS")
            y = drawTableHeader(canvas, y)

            if (rows.isEmpty()) {
                y = drawEmptyStateRow(canvas, y)
            } else {
                rows.forEachIndexed { index, row ->
                    if (y + TABLE_ROW_HEIGHT + TOTAL_ROW_HEIGHT + FOOTER_SPACE > PAGE_HEIGHT - MARGIN) {
                        drawFooter(canvas, pageNumber)
                        pdfDocument.finishPage(page)

                        pageNumber += 1
                        page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                        canvas = page.canvas
                        y = MARGIN.toFloat()

                        y = drawContinuationHeader(canvas, y, reportPeriod, generatedOn, pageNumber)
                        y += 12f
                        y = drawSectionTitle(canvas, y, "TRANSACTIONS")
                        y = drawTableHeader(canvas, y)
                    }
                    y = drawTableRow(canvas, y, row, index % 2 == 0)
                }
            }

            if (y + TOTAL_ROW_HEIGHT + FOOTER_SPACE > PAGE_HEIGHT - MARGIN) {
                drawFooter(canvas, pageNumber)
                pdfDocument.finishPage(page)

                pageNumber += 1
                page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = MARGIN.toFloat()

                y = drawContinuationHeader(canvas, y, reportPeriod, generatedOn, pageNumber)
                y += 12f
                y = drawSectionTitle(canvas, y, "TOTAL")
                y = drawTableHeader(canvas, y)
            }

            y = drawTotalsRow(canvas, y, totalIncome, totalExpense, closingBalance)
            drawFooter(canvas, pageNumber)

            pdfDocument.finishPage(page)
            file.outputStream().use { output -> pdfDocument.writeTo(output) }

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawReportHeader(canvas: Canvas, startY: Float, periodText: String, generatedOn: String): Float {
        val brandPaint = Paint().apply {
            textSize = 30f
            color = primaryColor
            isAntiAlias = true
            isFakeBoldText = true
        }
        val titlePaint = Paint().apply {
            textSize = 22f
            color = 0xFF1C1F2E.toInt()
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            textSize = 12f
            color = subtleTextColor
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            textSize = 15f
            color = 0xFF1C1F2E.toInt()
            isAntiAlias = true
            isFakeBoldText = true
        }
        val linePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = primaryColor
            isAntiAlias = true
        }

        var y = startY
        canvas.drawText("SpendRoute", SIDE_MARGIN, y + 8f, brandPaint)
        canvas.drawText("Expense Report", SIDE_MARGIN, y + 36f, titlePaint)

        val rightAlignedLabelPaint = Paint(labelPaint).apply { textAlign = Paint.Align.RIGHT }
        val rightAlignedValuePaint = Paint(valuePaint).apply { textAlign = Paint.Align.RIGHT }
        val rightEdge = PAGE_WIDTH - SIDE_MARGIN
        canvas.drawText("Report Period", rightEdge, y + 4f, rightAlignedLabelPaint)
        canvas.drawText(periodText, rightEdge, y + 28f, rightAlignedValuePaint)
        canvas.drawText("Generated On", rightEdge, y + 56f, rightAlignedLabelPaint)
        canvas.drawText(generatedOn, rightEdge, y + 80f, rightAlignedValuePaint)

        y += 100f
        canvas.drawLine(SIDE_MARGIN, y, PAGE_WIDTH - SIDE_MARGIN, y, linePaint)
        return y
    }

    private fun drawNameRow(canvas: Canvas, startY: Float, userName: String): Float {
        if (userName.isBlank()) return startY

        val labelPaint = Paint().apply {
            textSize = 11f
            color = subtleTextColor
            isAntiAlias = true
            isFakeBoldText = true
        }
        val valuePaint = Paint().apply {
            textSize = 11f
            color = 0xFF1C1F2E.toInt()
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = borderColor
            isAntiAlias = true
        }

        val rowTop = startY
        val rowBottom = rowTop + 26f
        canvas.drawText("Account Name:", SIDE_MARGIN, rowTop + 18f, labelPaint)
        drawLeftEllipsizedText(canvas, userName, SIDE_MARGIN + 42f, rowTop + 18f, PAGE_WIDTH - SIDE_MARGIN, valuePaint)
        canvas.drawLine(SIDE_MARGIN, rowBottom, PAGE_WIDTH - SIDE_MARGIN, rowBottom, linePaint)

        return rowBottom
    }

    private fun drawLeftEllipsizedText(
        canvas: Canvas,
        text: String,
        startX: Float,
        baselineY: Float,
        maxRight: Float,
        paint: Paint
    ) {
        val maxWidth = maxRight - startX
        var drawText = text
        if (paint.measureText(drawText) > maxWidth) {
            while (drawText.isNotEmpty() && paint.measureText("$drawText...") > maxWidth) {
                drawText = drawText.dropLast(1)
            }
            drawText = if (drawText.isEmpty()) "..." else "$drawText..."
        }
        canvas.drawText(drawText, startX, baselineY, paint)
    }

    private fun drawContinuationHeader(
        canvas: Canvas,
        startY: Float,
        periodText: String,
        generatedOn: String,
        pageNumber: Int
    ): Float {
        val titlePaint = Paint().apply {
            textSize = 18f
            color = primaryColor
            isAntiAlias = true
            isFakeBoldText = true
        }
        val subtitlePaint = Paint().apply {
            textSize = 12f
            color = subtleTextColor
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            color = borderColor
            isAntiAlias = true
        }

        canvas.drawText("SpendRoute Expense Report (Page $pageNumber)", SIDE_MARGIN, startY + 18f, titlePaint)
        canvas.drawText("Period: $periodText    Generated: $generatedOn", SIDE_MARGIN, startY + 36f, subtitlePaint)
        val lineY = startY + 48f
        canvas.drawLine(SIDE_MARGIN, lineY, PAGE_WIDTH - SIDE_MARGIN, lineY, linePaint)
        return lineY
    }

    private fun drawSummaryCards(
        canvas: Canvas,
        startY: Float,
        totalIncome: Double,
        totalExpense: Double,
        closingBalance: Double,
        transactionCount: Int
    ): Float {
        val sectionPaint = Paint().apply {
            textSize = 16f
            color = 0xFF232A3A.toInt()
            isAntiAlias = true
            isFakeBoldText = true
        }
        canvas.drawText("SUMMARY", SIDE_MARGIN, startY + 14f, sectionPaint)

        val cardTop = startY + 28f
        val cardHeight = 66f
        val gap = 10f
        val totalWidth = PAGE_WIDTH - (SIDE_MARGIN * 2f)
        val cardWidth = (totalWidth - 3f * gap) / 4f

        drawSummaryCard(canvas, SIDE_MARGIN, cardTop, cardWidth, cardHeight, "TOTAL INCOME", formatCurrency(totalIncome), 0xFF24A55A.toInt())
        drawSummaryCard(canvas, SIDE_MARGIN + cardWidth + gap, cardTop, cardWidth, cardHeight, "TOTAL EXPENSE", formatCurrency(totalExpense), 0xFFE53935.toInt())
        drawSummaryCard(canvas, SIDE_MARGIN + (cardWidth + gap) * 2f, cardTop, cardWidth, cardHeight, "BALANCE", formatCurrency(closingBalance), primaryColor)
        drawSummaryCard(canvas, SIDE_MARGIN + (cardWidth + gap) * 3f, cardTop, cardWidth, cardHeight, "TRANSACTIONS", transactionCount.toString(), 0xFF7A2CD0.toInt())

        return cardTop + cardHeight
    }

    private fun drawSummaryCard(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        label: String,
        value: String,
        accent: Int
    ) {
        val cardPaint = Paint().apply {
            color = 0xFFFDFDFF.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            textSize = 10f
            color = accent
            isAntiAlias = true
            isFakeBoldText = true
        }
        val valuePaint = Paint().apply {
            textSize = 15f
            color = accent
            isAntiAlias = true
            isFakeBoldText = true
        }

        val rect = RectF(left, top, left + width, top + height)
        canvas.drawRoundRect(rect, 8f, 8f, cardPaint)
        canvas.drawRoundRect(rect, 8f, 8f, borderPaint)
        canvas.drawText(label, left + 10f, top + 25f, labelPaint)
        canvas.drawText(value, left + 10f, top + 48f, valuePaint)
    }

    private fun drawSectionTitle(canvas: Canvas, startY: Float, title: String): Float {
        val paint = Paint().apply {
            textSize = 16f
            color = 0xFF232A3A.toInt()
            isAntiAlias = true
            isFakeBoldText = true
        }
        val y = startY + 14f
        canvas.drawText(title, SIDE_MARGIN, y, paint)
        return y + 12f
    }

    private fun drawTableHeader(canvas: Canvas, startY: Float): Float {
        val bgPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            textSize = 11f
            color = 0xFFFFFFFF.toInt()
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val linePaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val left = SIDE_MARGIN
        val right = PAGE_WIDTH - SIDE_MARGIN
        val top = startY
        val bottom = startY + TABLE_HEADER_HEIGHT

        canvas.drawRoundRect(RectF(left, top, right, bottom), 6f, 6f, bgPaint)

        var x = left
        tableColumns.forEachIndexed { index, column ->
            val centerX = x + (column.width / 2f)
            canvas.drawText(column.title, centerX, top + 22f, textPaint)
            x += column.width
            if (index != tableColumns.lastIndex) {
                canvas.drawLine(x, top + 4f, x, bottom - 4f, linePaint)
            }
        }

        return bottom
    }

    private fun drawTableRow(canvas: Canvas, startY: Float, row: StatementRow, shaded: Boolean): Float {
        val rowBgPaint = Paint().apply {
            color = if (shaded) 0xFFFAFBFF.toInt() else 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            textSize = 10.5f
            color = 0xFF1F2533.toInt()
            isAntiAlias = true
        }
        val subtlePaint = Paint().apply {
            textSize = 10f
            color = subtleTextColor
            isAntiAlias = true
        }
        val amountPaint = Paint().apply {
            textSize = 10.5f
            color = if (row.isIncome) 0xFF24A55A.toInt() else 0xFFE53935.toInt()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val balancePaint = Paint().apply {
            textSize = 10.5f
            color = 0xFF1F2533.toInt()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val typePaint = Paint().apply {
            textSize = 9.5f
            color = if (row.isIncome) 0xFF24A55A.toInt() else 0xFFE53935.toInt()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val left = SIDE_MARGIN
        val right = PAGE_WIDTH - SIDE_MARGIN
        val top = startY
        val bottom = startY + TABLE_ROW_HEIGHT

        canvas.drawRect(left, top, right, bottom, rowBgPaint)
        canvas.drawRect(left, top, right, bottom, borderPaint)

        var x = left
        tableColumns.forEachIndexed { index, column ->
            val cellLeft = x
            val cellRight = x + column.width
            if (index != 0) {
                canvas.drawLine(cellLeft, top, cellLeft, bottom, borderPaint)
            }
            when (index) {
                0 -> {
                    drawCenteredEllipsizedText(canvas, row.dateTime, cellLeft + 6f, top + 24f, cellRight - 6f, textPaint)
                }
                1 -> {
                    drawCenteredEllipsizedText(canvas, row.description, cellLeft + 6f, top + 24f, cellRight - 6f, textPaint)
                }
                2 -> {
                    drawCenteredEllipsizedText(canvas, row.category, cellLeft + 6f, top + 24f, cellRight - 6f, textPaint)
                }
                3 -> {
                    canvas.drawText(row.type, (cellLeft + cellRight) / 2f, top + 24f, typePaint)
                }
                4 -> {
                    canvas.drawText(formatSignedCurrency(row.amount, row.isIncome), (cellLeft + cellRight) / 2f, top + 24f, amountPaint)
                }
                5 -> {
                    canvas.drawText(formatCurrency(row.balance), (cellLeft + cellRight) / 2f, top + 24f, balancePaint)
                }
            }
            x += column.width
        }

        return bottom
    }

    private fun drawEmptyStateRow(canvas: Canvas, startY: Float): Float {
        val borderPaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            textSize = 11f
            color = subtleTextColor
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val top = startY
        val bottom = startY + TABLE_ROW_HEIGHT
        val left = SIDE_MARGIN
        val right = PAGE_WIDTH - SIDE_MARGIN
        canvas.drawRect(left, top, right, bottom, borderPaint)
        canvas.drawText("No transactions available for this report period", (left + right) / 2f, top + 24f, textPaint)
        return bottom
    }

    private fun drawTotalsRow(
        canvas: Canvas,
        startY: Float,
        totalIncome: Double,
        totalExpense: Double,
        closingBalance: Double
    ): Float {
        val bgPaint = Paint().apply {
            color = 0xFFF3F2FD.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            textSize = 12f
            color = primaryColor
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val totalTextPaint = Paint().apply {
            textSize = 11f
            color = subtleTextColor
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val incomePaint = Paint().apply {
            textSize = 12f
            color = 0xFF24A55A.toInt()
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val expensePaint = Paint().apply {
            textSize = 12f
            color = 0xFFE53935.toInt()
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val balancePaint = Paint().apply {
            textSize = 12f
            color = primaryColor
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val left = SIDE_MARGIN
        val right = PAGE_WIDTH - SIDE_MARGIN
        val top = startY
        val bottom = top + TOTAL_ROW_HEIGHT
        canvas.drawRect(left, top, right, bottom, bgPaint)
        canvas.drawRect(left, top, right, bottom, borderPaint)

        var x = left
        tableColumns.forEachIndexed { index, column ->
            val cellLeft = x
            val cellRight = x + column.width
            val centerX = (cellLeft + cellRight) / 2f

            if (index != 0) {
                canvas.drawLine(cellLeft, top, cellLeft, bottom, borderPaint)
            }

            when (index) {
                0 -> {
                    canvas.drawText("TOTAL", centerX, top + 30f, labelPaint)
                }
                1, 2 -> {
                    canvas.drawText("-", centerX, top + 30f, totalTextPaint)
                }
                3 -> {
                    canvas.drawText("-", centerX, top + 30f, totalTextPaint)
                }
                4 -> {
                    canvas.drawText("+ ${formatCurrency(totalIncome)}", centerX, top + 19f, incomePaint)
                    canvas.drawText("- ${formatCurrency(totalExpense)}", centerX, top + 38f, expensePaint)
                }
                5 -> {
                    canvas.drawText(formatCurrency(closingBalance), centerX, top + 30f, balancePaint)
                }
            }
            x += column.width
        }

        return bottom
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int) {
        val linePaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            textSize = 10f
            color = subtleTextColor
            isAntiAlias = true
        }
        val rightPaint = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }

        val lineY = (PAGE_HEIGHT - MARGIN - 30).toFloat()
        canvas.drawLine(SIDE_MARGIN, lineY, PAGE_WIDTH - SIDE_MARGIN, lineY, linePaint)
        canvas.drawText("Thank you for using SpendRoute", SIDE_MARGIN, lineY + 20f, textPaint)
        canvas.drawText("This is a system generated report", PAGE_WIDTH - SIDE_MARGIN, lineY + 20f, rightPaint)
        canvas.drawText("Page $pageNumber", PAGE_WIDTH - SIDE_MARGIN, lineY + 34f, rightPaint)
    }

    private fun drawCenteredEllipsizedText(
        canvas: Canvas,
        text: String,
        left: Float,
        baselineY: Float,
        right: Float,
        paint: Paint
    ) {
        val maxWidth = right - left
        var drawText = text
        if (paint.measureText(drawText) > maxWidth) {
            while (drawText.isNotEmpty() && paint.measureText("$drawText...") > maxWidth) {
                drawText = drawText.dropLast(1)
            }
            drawText = if (drawText.isEmpty()) "..." else "$drawText..."
        }

        val textWidth = paint.measureText(drawText)
        val centeredX = left + ((maxWidth - textWidth) / 2f).coerceAtLeast(0f)
        canvas.drawText(drawText, centeredX, baselineY, paint)
    }

    private fun buildRowsWithRunningBalance(transactions: List<Transaction>): List<StatementRow> {
         var runningBalance = 0.0
         return transactions.map { txn ->
             runningBalance += if (txn.isIncome) txn.amount else -txn.amount
             StatementRow(
                 dateTime = txn.date,
                 description = txn.note.takeIf { !it.isNullOrEmpty() } ?: "-", // Show notes or "-" if not available
                 category = txn.category,
                 type = if (txn.isIncome) "INCOME" else "EXPENSE",
                 amount = txn.amount,
                 balance = runningBalance,
                 isIncome = txn.isIncome
             )
         }
     }

    private fun parseTransactionTime(transaction: Transaction): Long {
        val dateTimeText = "${transaction.date} ${transaction.time}".trim()
        dateTimeParsers.forEach { parser ->
            try {
                parser.isLenient = true
                val parsed = parser.parse(dateTimeText)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
            }
        }
        return transaction.id.toLongOrNull() ?: Long.MAX_VALUE
    }

    private fun buildReportPeriod(transactions: List<Transaction>): String {
        if (transactions.isEmpty()) {
            return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        }
        val minTime = transactions.minOf { parseTransactionTime(it) }
        val maxTime = transactions.maxOf { parseTransactionTime(it) }
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val start = formatter.format(Date(minTime))
        val end = formatter.format(Date(maxTime))
        return if (start == end) start else "$start - $end"
    }

    private fun formatCurrency(value: Double): String {
        return "Rs ${String.format(Locale.US, "%,.2f", abs(value))}"
    }

    private fun formatSignedCurrency(value: Double, isIncome: Boolean): String {
        val sign = if (isIncome) "+" else "-"
        return "$sign ${formatCurrency(value)}"
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


