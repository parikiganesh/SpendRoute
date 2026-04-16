package com.parikiganesh.spendroute.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Category options for transactions
 * Contains all income and expense categories used across the app
 * 
 * MVVM Best Practice:
 * - Kept in Data layer (not in Composable)
 * - Centralized for easy maintenance
 * - Reusable across multiple screens
 */

data class CategoryOption(
    val name: String,
    val icon: ImageVector,
    val type: TransactionType
)

enum class TransactionType {
    INCOME, EXPENSE
}

object CategoryConstants {
    val incomeCategories = listOf(
        CategoryOption("Salary", Icons.Default.AttachMoney, TransactionType.INCOME),
        CategoryOption("Freelance", Icons.Default.Work, TransactionType.INCOME),
        CategoryOption("Investment", Icons.AutoMirrored.Filled.TrendingUp, TransactionType.INCOME),
        CategoryOption("Bonus", Icons.Default.CardGiftcard, TransactionType.INCOME),
        CategoryOption("Commission", Icons.Default.Money, TransactionType.INCOME),
        CategoryOption("Other", Icons.Default.Extension, TransactionType.INCOME)
    )

    val expenseCategories = listOf(
        CategoryOption("Food", Icons.Default.Dining, TransactionType.EXPENSE),
        CategoryOption("Travel", Icons.Default.DirectionsCar, TransactionType.EXPENSE),
        CategoryOption("Entertainment", Icons.Default.Movie, TransactionType.EXPENSE),
        CategoryOption("Bills", Icons.Default.ElectricBolt, TransactionType.EXPENSE),
        CategoryOption("Shopping", Icons.Default.ShoppingCart, TransactionType.EXPENSE),
        CategoryOption("Healthcare", Icons.Default.LocalHospital, TransactionType.EXPENSE),
        CategoryOption("Education", Icons.Default.School, TransactionType.EXPENSE),
        CategoryOption("Utilities", Icons.Default.Construction, TransactionType.EXPENSE),
        CategoryOption("Other", Icons.Default.Extension, TransactionType.EXPENSE)
    )
    
    /**
     * Get categories based on transaction type
     */
    fun getCategories(type: TransactionType): List<CategoryOption> {
        return when (type) {
            TransactionType.INCOME -> incomeCategories
            TransactionType.EXPENSE -> expenseCategories
        }
    }

    /**
     * Get icon for a specific category name
     * Searches both income and expense categories
     * Returns Extension icon as fallback for unknown categories
     */
    fun getCategoryIcon(categoryName: String): ImageVector {
        // Search in income categories
        incomeCategories.find { it.name == categoryName }?.let { return it.icon }
        
        // Search in expense categories
        expenseCategories.find { it.name == categoryName }?.let { return it.icon }
        
        // Fallback to Extension icon for unknown categories
        return Icons.Default.Extension
    }
}

