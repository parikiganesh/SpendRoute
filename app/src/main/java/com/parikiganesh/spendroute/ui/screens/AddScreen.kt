package com.parikiganesh.spendroute.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.parikiganesh.spendroute.R
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.data.model.TransactionType
import com.parikiganesh.spendroute.data.model.CategoryConstants
import com.parikiganesh.spendroute.ui.components.GreetingHeader
import com.parikiganesh.spendroute.ui.theme.LocalTypography
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.viewmodel.AddTransactionViewModel
import com.parikiganesh.spendroute.viewmodel.factory.AddTransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    modifier: Modifier = Modifier,
    onTransactionAdded: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    transactionToEdit: Transaction? = null,
    onClearEdit: () -> Unit = {},
    viewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModelFactory(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    // Collect form state from ViewModel
    val formState = viewModel.formState.collectAsState()
    val state = formState.value
    
    // Initialize form with transaction data if editing
    LaunchedEffect(transactionToEdit) {
        if (transactionToEdit != null) {
            viewModel.prepareEditTransaction(transactionToEdit)
        } else {
            viewModel.resetForm()
        }
    }
    
    // Date picker state (UI-only local state)
    val showDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Get categories based on transaction type from state
    val categories = CategoryConstants.getCategories(state.transactionType)
    val isEditing = transactionToEdit != null
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7FC))
    ) {
        // Fixed Purple Header
        GreetingHeader(
            title = if (isEditing) {
                if (state.transactionType == TransactionType.INCOME) stringResource(R.string.edit_income) else stringResource(R.string.edit_expense)
            } else {
                if (state.transactionType == TransactionType.INCOME) stringResource(R.string.add_income) else stringResource(R.string.add_expense)
            },
            subtitle = if (isEditing) {
                stringResource(R.string.update_transaction_details)
            } else {
                if (state.transactionType == TransactionType.INCOME) stringResource(R.string.record_earnings) else stringResource(R.string.record_spending)
            },
            showAvatar = false
        )

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F7FC))
        ) {
            // Income/Expense Switch
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = state.transactionType == TransactionType.EXPENSE,
                            onClick = { viewModel.switchTransactionType(TransactionType.EXPENSE) },
                            shape = RoundedCornerShape(
                                topStart = 8.dp,
                                bottomStart = 8.dp,
                                topEnd = 0.dp,
                                bottomEnd = 0.dp
                            ),
                            modifier = Modifier.weight(1f),
                            icon = { },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Color(0xFF5B4B9B),
                                activeContentColor = Color.White,
                                inactiveContainerColor = Color(0xFFF5F5F5),
                                inactiveContentColor = Color(0xFF5B4B9B)
                            )
                        ) {
                            Text(stringResource(R.string.minus_expense))
                        }
                        SegmentedButton(
                            selected = state.transactionType == TransactionType.INCOME,
                            onClick = { viewModel.switchTransactionType(TransactionType.INCOME) },
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                bottomStart = 0.dp,
                                topEnd = 8.dp,
                                bottomEnd = 8.dp
                            ),
                            modifier = Modifier.weight(1f),
                            icon = { },
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Color(0xFF5B4B9B),
                                activeContentColor = Color.White,
                                inactiveContainerColor = Color(0xFFF5F5F5),
                                inactiveContentColor = Color(0xFF5B4B9B)
                            )
                        ) {
                            Text(stringResource(R.string.plus_income))
                        }
                    }
                }
            }

            // Amount Input Card
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.amount),
                            style = LocalTypography.current.bodySmallNormal,
                            color = Color.DarkGray
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.currency),
                                style = LocalTypography.current.bodyMediumPrimary,
                                color = Color.Black,
                                fontSize = 36.sp
                            )
                            TextField(
                                value = state.amount,
                                onValueChange = { viewModel.updateAmount(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Transparent),
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.enter_amount),
                                        style = LocalTypography.current.bodyExtraLargeText.copy(
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFFBDBDBD)
                                    )
                                },
                                textStyle = LocalTypography.current.bodyExtraLargeText.copy(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Category Selection Card
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.source),
                            style = LocalTypography.current.bodySmallNormal,
                            color = Color.DarkGray
                        )
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .border(
                                            width = if (state.selectedCategory == category.name) 2.dp else 0.dp,
                                            color = if (state.selectedCategory == category.name) Color(0xFF4DB8A8) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.updateCategory(category.name) },
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = if (state.selectedCategory == category.name) Color(0xFFE0F7F6) else Color(0xFFF5F5F5)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = category.name,
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Black
                                        )
                                        Text(
                                            text = category.name,
                                            style = LocalTypography.current.bodySmallNormal,
                                            color = Color(0xFF5B4B9B),
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Note (Optional) Card
            item {
                val notePlaceholder = if (state.transactionType == TransactionType.INCOME) 
                    stringResource(R.string.note_placeholder_income)
                else 
                    stringResource(R.string.note_placeholder_expense)
                
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.note_optional),
                            style = LocalTypography.current.bodySmallNormal,
                            color = Color.DarkGray
                        )
                        TextField(
                            value = state.note,
                            onValueChange = { viewModel.updateNote(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent),
                            placeholder = {
                                Text(
                                    text = notePlaceholder,
                                    style = LocalTypography.current.bodyMediumRegular,
                                    color = Color(0xFFBDBDBD)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Date Card with DatePicker Dialog
            item {
                if (showDatePicker.value) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker.value = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val date = Date(millis)
                                        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                                        viewModel.updateDate(dateFormat.format(date))
                                    }
                                    showDatePicker.value = false
                                }
                            ) {
                                Text(stringResource(R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker.value = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { showDatePicker.value = true },
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.date),
                            style = LocalTypography.current.bodySmallNormal,
                            color = Color.DarkGray
                        )
                        Text(
                            text = state.selectedDate,
                            style = LocalTypography.current.bodyMediumPrimary,
                            color = Color(0xFF5B4B9B)
                        )
                    }
                }
            }

            // Save Button
            item {
                val transactionSavedText = stringResource(R.string.transaction_saved)
                val transactionUpdatedText = stringResource(R.string.transaction_updated)
                
                Button(
                    onClick = {
                        if (isEditing) {
                            viewModel.updateTransaction(transactionToEdit.id)
                            Toast.makeText(context, transactionUpdatedText, Toast.LENGTH_SHORT).show()
                            onClearEdit()
                            onNavigateToHome()
                        } else {
                            viewModel.saveTransaction()
                            Toast.makeText(context, transactionSavedText, Toast.LENGTH_SHORT).show()
                            onTransactionAdded()
                            onNavigateToHome()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B4B9B),
                        disabledContainerColor = Color(0xFFB8B3E5)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = state.isFormValid && !state.isLoading
                ) {
                    Text(
                        text = if (isEditing) {
                            if (state.transactionType == TransactionType.INCOME) stringResource(R.string.update_income) else stringResource(R.string.update_expense)
                        } else {
                            if (state.transactionType == TransactionType.INCOME) stringResource(R.string.save_income) else stringResource(R.string.save_expense)
                        },
                        style = LocalTypography.current.headingSmallSemibold,
                        color = Color.White
                    )
                }
            }

            // Bottom spacing for navigation bar
            item {
                Box(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    SpendRouteTheme {
        AddTransactionScreen()
    }
}

