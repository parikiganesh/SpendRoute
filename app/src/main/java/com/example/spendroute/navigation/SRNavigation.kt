package com.example.spendroute.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.spendroute.R

sealed class SRNavigation(val route: String) {
    data object Home : SRNavigation("home")
    data object Transactions : SRNavigation("transactions")
    data object AddExpense : SRNavigation("add_expense")
    data object Analytics : SRNavigation("analytics")
    data object Profile : SRNavigation("profile")
}

data class NavItem(
    val labelResId: Int,
    val icon: ImageVector,
    val route: SRNavigation
)

object NavigationGraph {
    val navigationItems = listOf(
        NavItem(R.string.tab_home, Icons.Default.Home, SRNavigation.Home),
        NavItem(R.string.tab_transactions, Icons.AutoMirrored.Filled.List, SRNavigation.Transactions),
        NavItem(R.string.tab_add, Icons.Default.Add, SRNavigation.AddExpense),
        NavItem(R.string.tab_analytics, Icons.Default.BarChart, SRNavigation.Analytics),
        NavItem(R.string.tab_profile, Icons.Default.Person, SRNavigation.Profile)
    )
}


