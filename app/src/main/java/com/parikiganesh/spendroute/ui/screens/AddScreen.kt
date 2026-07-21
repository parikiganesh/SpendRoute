package com.parikiganesh.spendroute.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.filled.Delete
import androidx.core.content.FileProvider
import com.parikiganesh.spendroute.R
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.data.model.TransactionType
import com.parikiganesh.spendroute.data.model.CategoryConstants
import com.parikiganesh.spendroute.ui.components.GreetingHeader
import com.parikiganesh.spendroute.ui.theme.LocalTypography
import com.parikiganesh.spendroute.ui.theme.SpendRouteTheme
import com.parikiganesh.spendroute.viewmodel.AddTransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    modifier: Modifier = Modifier,
    onTransactionAdded: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    transactionToEdit: Transaction? = null,
    initialTransactionType: TransactionType? = null,
    onClearEdit: () -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel()
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
            // If initial transaction type is specified, update the form to use it
            if (initialTransactionType != null) {
                viewModel.switchTransactionType(initialTransactionType)
            }
        }
    }
    
    // Handle back button - clear edit state when going back
    BackHandler {
        onClearEdit()
        onNavigateToHome()
    }

    // Date picker state (UI-only local state)
    val showDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Get categories based on transaction type from state
    val categories = CategoryConstants.getCategories(state.transactionType)
    val isEditing = transactionToEdit != null
    val context = LocalContext.current
    val showReceiptSourceDialog = remember { mutableStateOf(false) }
    val pendingCameraUri = remember { mutableStateOf<Uri?>(null) }

    val receiptPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.updateReceiptUri(uri?.toString())
    }
    val receiptCaptureFailedText = stringResource(R.string.receipt_capture_failed)
    val receiptCaptureUnavailableText = stringResource(R.string.receipt_capture_unavailable)
    val cameraReceiptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.updateReceiptUri(pendingCameraUri.value?.toString())
        } else {
            Toast.makeText(context, receiptCaptureFailedText, Toast.LENGTH_SHORT).show()
        }
        pendingCameraUri.value = null
    }
    val transactionSavedText = stringResource(R.string.transaction_saved)
    val transactionUpdatedText = stringResource(R.string.transaction_updated)

    LaunchedEffect(state.validationError) {
        state.validationError?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.saveCompleted) {
        if (state.saveCompleted) {
            val successText = if (isEditing) transactionUpdatedText else transactionSavedText
            Toast.makeText(context, successText, Toast.LENGTH_SHORT).show()
            if (isEditing) {
                onClearEdit()
            } else {
                onTransactionAdded()
            }
            onNavigateToHome()
            viewModel.clearSaveCompleted()
        }
    }

    if (showReceiptSourceDialog.value) {
        AlertDialog(
            onDismissRequest = { showReceiptSourceDialog.value = false },
            title = { Text(text = stringResource(R.string.attach_receipt_optional)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            showReceiptSourceDialog.value = false
                            val captureUri = createReceiptTempUri(context)
                            if (captureUri == null) {
                                Toast.makeText(context, receiptCaptureUnavailableText, Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            pendingCameraUri.value = captureUri
                            cameraReceiptLauncher.launch(captureUri)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = Color(0xFF5B4B9B)
                        )
                        Text(
                            text = stringResource(R.string.take_photo),
                            modifier = Modifier.padding(start = 8.dp),
                            color = Color(0xFF5B4B9B)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            showReceiptSourceDialog.value = false
                            receiptPickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = null,
                            tint = Color(0xFF5B4B9B)
                        )
                        Text(
                            text = stringResource(R.string.choose_from_photos),
                            modifier = Modifier.padding(start = 8.dp),
                            color = Color(0xFF5B4B9B)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReceiptSourceDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

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
                            style = LocalTypography.current.bodyLargeNormal,
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
                                fontSize = 26.sp
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
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFFBDBDBD)
                                    )
                                },
                                textStyle = LocalTypography.current.bodyExtraLargeText.copy(
                                    fontSize = 26.sp,
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
                            style = LocalTypography.current.bodyLargeNormal,
                            color = Color.DarkGray
                        )
                        
                        val categoryScrollState = rememberLazyListState()
                        
                        LazyRow(
                            state = categoryScrollState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .padding(bottom = 12.dp), // Space for indicator
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
                                            color = Color.Black,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Custom Scroll Indicator / Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color(0xFFEEEEEE), RoundedCornerShape(2.dp))
                                .drawWithContent {
                                    drawContent()
                                    
                                    val layoutInfo = categoryScrollState.layoutInfo
                                    val visibleItems = layoutInfo.visibleItemsInfo
                                    
                                    if (visibleItems.isNotEmpty()) {
                                        val viewportWidth = layoutInfo.viewportEndOffset.toFloat() - layoutInfo.viewportStartOffset.toFloat()
                                        
                                        // Total content width calculation
                                        // We use the last item's end offset as the total content width
                                        val totalItems = layoutInfo.totalItemsCount
                                        val lastItem = visibleItems.last()
                                        val firstItem = visibleItems.first()
                                        
                                        // Estimate total width if not all items are visible
                                        val averageItemSize = (lastItem.offset + lastItem.size - firstItem.offset).toFloat() / visibleItems.size
                                        val contentWidth = averageItemSize * totalItems
                                        
                                        if (contentWidth > viewportWidth) {
                                            val indicatorWidth = (viewportWidth / contentWidth) * viewportWidth
                                            
                                            // Calculate scroll progress based on the current scroll position
                                            val scrollOffset = categoryScrollState.firstVisibleItemIndex * averageItemSize + categoryScrollState.firstVisibleItemScrollOffset
                                            val maxScrollOffset = contentWidth - viewportWidth
                                            
                                            val scrollProgress = if (maxScrollOffset > 0) (scrollOffset / maxScrollOffset).coerceIn(0f, 1f) else 0f
                                            val indicatorOffset = scrollProgress * (viewportWidth - indicatorWidth)
                                            
                                            drawRoundRect(
                                                color = Color(0xFF5B4B9B),
                                                topLeft = Offset(indicatorOffset, 0f),
                                                size = Size(indicatorWidth, size.height),
                                                cornerRadius = CornerRadius(2.dp.toPx())
                                            )
                                        }
                                    }
                                }
                        )
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
                            style = LocalTypography.current.bodyLargeNormal,
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
                                    style = LocalTypography.current.bodyLargeNormal,
                                    color = Color(0xFFBDBDBD)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Receipt attachment card
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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.attach_receipt_optional),
                            style = LocalTypography.current.bodyLargeNormal,
                            color = Color.DarkGray
                        )

                        OutlinedButton(
                            onClick = { showReceiptSourceDialog.value = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AttachFile,
                                contentDescription = null,
                                tint = Color(0xFF5B4B9B)
                            )
                            Text(
                                text = if (state.receiptImageUri == null) {
                                    stringResource(R.string.choose_receipt_image)
                                } else {
                                    stringResource(R.string.change_receipt_image)
                                },
                                modifier = Modifier.padding(start = 8.dp),
                                color = Color(0xFF5B4B9B)
                            )
                        }

                        state.receiptImageUri?.let {
                            Text(
                                text = stringResource(R.string.receipt_attached),
                                style = LocalTypography.current.bodySmallNormal,
                                color = Color(0xFF2E7D32)
                            )

                            OutlinedButton(
                                onClick = { viewModel.updateReceiptUri(null) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828)
                                )
                                Text(
                                    text = stringResource(R.string.remove_receipt),
                                    modifier = Modifier.padding(start = 8.dp),
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
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
                            style = LocalTypography.current.bodyLargeNormal,
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
                Button(
                    onClick = {
                        if (isEditing) {
                            viewModel.updateTransaction(transactionToEdit.id)
                        } else {
                            viewModel.saveTransaction()
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

private fun createReceiptTempUri(context: Context): Uri? {
    return runCatching {
        val receiptsDir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val tempFile = File.createTempFile("receipt_", ".jpg", receiptsDir)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }.getOrNull()
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    SpendRouteTheme {
        AddTransactionScreen()
    }
}

