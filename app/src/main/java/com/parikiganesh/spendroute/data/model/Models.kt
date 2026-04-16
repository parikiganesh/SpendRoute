package com.parikiganesh.spendroute.data.model

data class BalanceInfo(
    val month: String,
    val totalBalance: Double,
    val income: Double,
    val expense: Double,
    val budget: Double,
    val usedPercentage: Float
)

data class CategoryExpense(
    val name: String,
    val amount: Double
)

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: Double,
    val date: String,
    val time: String,
    val isIncome: Boolean,
    val icon: Int? = null,
    val note: String? = null  // Optional note field for transactions
)

@Suppress("UNUSED")
data class UserProfile(
    val name: String,
    val initials: String,
    val avatarColor: Long = 0xFF7C6FD4
)

