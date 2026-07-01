package com.parikiganesh.spendroute.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parikiganesh.spendroute.data.model.Transaction
import com.parikiganesh.spendroute.data.model.TransactionType
import com.parikiganesh.spendroute.repository.TransactionRepository
import com.parikiganesh.spendroute.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

/**
 * AddTransactionViewModel manages the form state and business logic for adding/editing transactions.
 * Follows MVVM pattern by separating UI state from presentation logic.
 */
data class AddTransactionFormState(
    val amount: String = "",
    val selectedCategory: String? = null,
    val note: String = "",
    val selectedDate: String = "", // Will be set to current date in ViewModel init
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val isLoading: Boolean = false,
    val validationError: String? = null,
    val isFormValid: Boolean = false,
    val saveCompleted: Boolean = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    // Form state
    private val _formState = MutableStateFlow(AddTransactionFormState(selectedDate = DateTimeUtils.getCurrentDate()))
    val formState: StateFlow<AddTransactionFormState> = _formState.asStateFlow()

    /**
     * Update the amount field and revalidate form
     */
    fun updateAmount(amount: String) {
        _formState.value = _formState.value.copy(amount = amount)
        validateForm()
    }

    /**
     * Update the selected category and revalidate form
     */
    fun updateCategory(category: String?) {
        _formState.value = _formState.value.copy(selectedCategory = category)
        validateForm()
    }

    /**
     * Update the note field
     */
    fun updateNote(note: String) {
        _formState.value = _formState.value.copy(note = note)
    }

    /**
     * Update the selected date
     */
    fun updateDate(date: String) {
        _formState.value = _formState.value.copy(selectedDate = date)
    }

    /**
     * Switch between Income and Expense and reset form
     */
    fun switchTransactionType(type: TransactionType) {
        _formState.value = AddTransactionFormState(
            transactionType = type,
            selectedDate = DateTimeUtils.getCurrentDate()
        )
        validateForm()
    }

    /**
     * Validate form and update validation state
     */
    private fun validateForm() {
        val state = _formState.value
        val isValid = state.amount.isNotEmpty() &&
                state.selectedCategory != null &&
                state.amount.toDoubleOrNull() != null &&
                state.amount.toDouble() > 0

        _formState.value = state.copy(
            isFormValid = isValid,
            saveCompleted = false,
            validationError = when {
                state.amount.isEmpty() -> null
                state.selectedCategory == null -> null
                state.amount.toDoubleOrNull() == null -> "Invalid amount format"
                state.amount.toDouble() <= 0 -> "Amount must be greater than 0"
                else -> null
            }
        )
    }

    /**
     * Save a new transaction
     */
    fun saveTransaction() {
        val state = _formState.value
        if (!state.isFormValid) {
            _formState.value = state.copy(validationError = "Please fill in all required fields")
            return
        }

        _formState.value = state.copy(isLoading = true)

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            category = state.selectedCategory!!,
            amount = state.amount.toDouble(),
            date = state.selectedDate,
            time = DateTimeUtils.getCurrentTime(),
            note = state.note.ifEmpty { null },
            isIncome = state.transactionType == TransactionType.INCOME
        )

        viewModelScope.launch {
            try {
                repository.insertTransaction(transaction)
                _formState.value = AddTransactionFormState(
                    selectedDate = DateTimeUtils.getCurrentDate(),
                    saveCompleted = true
                )
            } catch (e: Exception) {
                val message = if (e is IOException && e.message == "No internet connection") {
                    "No internet connection"
                } else {
                    "Error saving transaction: ${e.message}"
                }
                _formState.value = state.copy(
                    isLoading = false,
                    validationError = message
                )
            }
        }
    }

    /**
     * Update an existing transaction
     */
    fun updateTransaction(id: String) {
        val state = _formState.value
        if (!state.isFormValid) {
            _formState.value = state.copy(validationError = "Please fill in all required fields")
            return
        }

        _formState.value = state.copy(isLoading = true)

        val transaction = Transaction(
            id = id,
            category = state.selectedCategory!!,
            amount = state.amount.toDouble(),
            date = state.selectedDate,
            time = DateTimeUtils.getCurrentTime(),
            note = state.note.ifEmpty { null },
            isIncome = state.transactionType == TransactionType.INCOME
        )

        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                _formState.value = AddTransactionFormState(
                    selectedDate = DateTimeUtils.getCurrentDate(),
                    saveCompleted = true
                )
            } catch (e: Exception) {
                val message = if (e is IOException && e.message == "No internet connection") {
                    "No internet connection"
                } else {
                    "Error updating transaction: ${e.message}"
                }
                _formState.value = state.copy(
                    isLoading = false,
                    validationError = message
                )
            }
        }
    }

    /**
     * Pre-populate form with transaction data for editing
     */
    fun prepareEditTransaction(transaction: Transaction) {
        _formState.value = AddTransactionFormState(
            amount = transaction.amount.toString(),
            selectedCategory = transaction.category,
            note = transaction.note ?: "",
            selectedDate = transaction.date,
            transactionType = if (transaction.isIncome) TransactionType.INCOME else TransactionType.EXPENSE
        )
        validateForm()
    }

    /**
     * Reset form to initial state
     */
    fun resetForm() {
        _formState.value = AddTransactionFormState(
            selectedDate = DateTimeUtils.getCurrentDate()
        )
    }

    fun clearSaveCompleted() {
        _formState.value = _formState.value.copy(saveCompleted = false)
    }
}

