package com.example.rentr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentr.model.TransactionModel
import com.example.rentr.repository.TransactionRepo
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepo) : ViewModel() {

    private val _transactionResult = MutableLiveData<Pair<Boolean, String?>>()
    val transactionResult: LiveData<Pair<Boolean, String?>> = _transactionResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // --- NEW: LiveData to hold the PIDX ---
    private val _pidx = MutableLiveData<String?>()
    val pidx: LiveData<String?> = _pidx

    fun addTransaction(transaction: TransactionModel) {
        _isLoading.value = true
        repository.addTransaction(transaction) { success, message ->
            _isLoading.value = false
            _transactionResult.value = Pair(success, message)
        }
    }

    // --- NEW: Function called by CheckoutActivity to start payment ---
    fun initiateKhaltiPayment(rentalPrice: Double, productId: String, productName: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Call the repo to get pidx from backend
            val resultPidx = repository.getPidxFromBackend(rentalPrice, productId, productName)

            _isLoading.value = false

            if (resultPidx != null) {
                // Success: Update LiveData so Activity can open Khalti
                _pidx.value = resultPidx
            } else {
                // Failure: Notify user
                _transactionResult.value = Pair(false, "Failed to connect to Payment Backend")
            }
        }
    }
}