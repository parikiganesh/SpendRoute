package com.parikiganesh.spendroute.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.spendroute.data.model.CategoryExpense
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.repository.TransactionRepository
import com.parikiganesh.spendroute.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AnalyticsData(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val incomeChangePercent: Float = 0f,
    val expenseChangePercent: Float = 0f,
    val monthlyData: List<MonthlyChartData> = emptyList(),
    val categoryExpenses: List<CategoryExpense> = emptyList()
)

data class MonthlyChartData(
    val month: String,
    val income: Double,
    val expense: Double
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _analyticsData = MutableStateFlow<AnalyticsData>(AnalyticsData())
    val analyticsData: StateFlow<AnalyticsData> = _analyticsData.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("Monthly")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    init {
        loadAnalyticsData()
    }

    fun selectPeriod(period: String) {
        _selectedPeriod.value = period
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            repository.getAllTransactions().collect { transactions ->
                when (_selectedPeriod.value) {
                    "Yearly" -> loadYearlyData(transactions)
                    else -> loadMonthlyData(transactions) // Monthly is default
                }
            }
        }
    }

    private fun loadMonthlyData(transactions: List<Transaction>) {
        // Get current month and last month
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        // Calculate current month data
        val currentMonthTransactions = transactions.filter { transaction ->
            val transactionMonth = DateTimeUtils.getMonthFromDate(transaction.date)
            val transactionYear = DateTimeUtils.getYearFromDate(transaction.date)
            transactionMonth == currentMonth && transactionYear == currentYear
        }

        val currentIncome = currentMonthTransactions
            .filter { it.isIncome }
            .sumOf { it.amount }

        val currentExpense = currentMonthTransactions
            .filter { !it.isIncome }
            .sumOf { it.amount }

        // Calculate last month data for comparison
        val lastMonthDate = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val lastMonth = lastMonthDate.get(Calendar.MONTH) + 1
        val lastYear = lastMonthDate.get(Calendar.YEAR)

        val lastMonthTransactions = transactions.filter { transaction ->
            val transactionMonth = DateTimeUtils.getMonthFromDate(transaction.date)
            val transactionYear = DateTimeUtils.getYearFromDate(transaction.date)
            transactionMonth == lastMonth && transactionYear == lastYear
        }

        val lastIncome = lastMonthTransactions
            .filter { it.isIncome }
            .sumOf { it.amount }

        val lastExpense = lastMonthTransactions
            .filter { !it.isIncome }
            .sumOf { it.amount }

        // Calculate percentage changes
        val incomeChangePercent = if (lastIncome > 0) {
            ((currentIncome - lastIncome) / lastIncome * 100).toFloat()
        } else {
            0f
        }

        val expenseChangePercent = if (lastExpense > 0) {
            ((currentExpense - lastExpense) / lastExpense * 100).toFloat()
        } else {
            0f
        }

        // Get last 6 months data for chart
        val last6MonthsData = mutableListOf<MonthlyChartData>()
        for (i in 5 downTo 0) {
            val monthCalendar = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val month = monthCalendar.get(Calendar.MONTH) + 1
            val year = monthCalendar.get(Calendar.YEAR)
            val monthName = DateTimeUtils.getMonthName(month)

            val monthTransactions = transactions.filter { transaction ->
                val transactionMonth = DateTimeUtils.getMonthFromDate(transaction.date)
                val transactionYear = DateTimeUtils.getYearFromDate(transaction.date)
                transactionMonth == month && transactionYear == year
            }

            val monthIncome = monthTransactions.filter { it.isIncome }.sumOf { it.amount }
            val monthExpense = monthTransactions.filter { !it.isIncome }.sumOf { it.amount }

            last6MonthsData.add(
                MonthlyChartData(
                    month = monthName.take(3),
                    income = monthIncome,
                    expense = monthExpense
                )
            )
        }

        // Calculate category expenses
        val categoryMap = mutableMapOf<String, Double>()
        currentMonthTransactions.filter { !it.isIncome }.forEach { transaction ->
            val current = categoryMap[transaction.category] ?: 0.0
            categoryMap[transaction.category] = current + transaction.amount
        }

        val categoryExpenses = categoryMap.map { (category, amount) ->
            CategoryExpense(name = category, amount = amount)
        }.sortedByDescending { it.amount }

        _analyticsData.value = AnalyticsData(
            totalIncome = currentIncome,
            totalExpense = currentExpense,
            incomeChangePercent = incomeChangePercent,
            expenseChangePercent = expenseChangePercent,
            monthlyData = last6MonthsData,
            categoryExpenses = categoryExpenses
        )
    }

    private fun loadYearlyData(transactions: List<Transaction>) {
        // Get current year and last year
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val lastYear = currentYear - 1

        // Calculate current year data
        val currentYearTransactions = transactions.filter { transaction ->
            val transactionYear = DateTimeUtils.getYearFromDate(transaction.date)
            transactionYear == currentYear
        }

        val currentIncome = currentYearTransactions
            .filter { it.isIncome }
            .sumOf { it.amount }

        val currentExpense = currentYearTransactions
            .filter { !it.isIncome }
            .sumOf { it.amount }

        // Calculate last year data for comparison
        val lastYearTransactions = transactions.filter { transaction ->
            val transactionYear = DateTimeUtils.getYearFromDate(transaction.date)
            transactionYear == lastYear
        }

        val lastIncome = lastYearTransactions
            .filter { it.isIncome }
            .sumOf { it.amount }

        val lastExpense = lastYearTransactions
            .filter { !it.isIncome }
            .sumOf { it.amount }

        // Calculate percentage changes
        val incomeChangePercent = if (lastIncome > 0) {
            ((currentIncome - lastIncome) / lastIncome * 100).toFloat()
        } else {
            0f
        }

        val expenseChangePercent = if (lastExpense > 0) {
            ((currentExpense - lastExpense) / lastExpense * 100).toFloat()
        } else {
            0f
        }

        // Get last 6 months data within current year for chart
        val last6MonthsData = mutableListOf<MonthlyChartData>()
        for (i in 5 downTo 0) {
            val monthCalendar = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val month = monthCalendar.get(Calendar.MONTH) + 1
            val year = monthCalendar.get(Calendar.YEAR)
            val monthName = DateTimeUtils.getMonthName(month)

            val monthTransactions = transactions.filter { transaction ->
                val transactionMonth = DateTimeUtils.getMonthFromDate(transaction.date)
                val transactionYear = DateTimeUtils.getYearFromDate(transaction.date)
                transactionMonth == month && transactionYear == year
            }

            val monthIncome = monthTransactions.filter { it.isIncome }.sumOf { it.amount }
            val monthExpense = monthTransactions.filter { !it.isIncome }.sumOf { it.amount }

            last6MonthsData.add(
                MonthlyChartData(
                    month = monthName.take(3),
                    income = monthIncome,
                    expense = monthExpense
                )
            )
        }

        // Calculate category expenses for current year
        val categoryMap = mutableMapOf<String, Double>()
        currentYearTransactions.filter { !it.isIncome }.forEach { transaction ->
            val current = categoryMap[transaction.category] ?: 0.0
            categoryMap[transaction.category] = current + transaction.amount
        }

        val categoryExpenses = categoryMap.map { (category, amount) ->
            CategoryExpense(name = category, amount = amount)
        }.sortedByDescending { it.amount }

        _analyticsData.value = AnalyticsData(
            totalIncome = currentIncome,
            totalExpense = currentExpense,
            incomeChangePercent = incomeChangePercent,
            expenseChangePercent = expenseChangePercent,
            monthlyData = last6MonthsData,
            categoryExpenses = categoryExpenses
        )
    }
}



