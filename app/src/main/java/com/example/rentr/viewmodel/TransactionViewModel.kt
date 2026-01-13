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

    private val _renterTransactions = MutableLiveData<List<TransactionModel>>()
    val renterTransactions: LiveData<List<TransactionModel>> = _renterTransactions

    private val _sellerTransactions = MutableLiveData<List<TransactionModel>>()
    val sellerTransactions: LiveData<List<TransactionModel>> = _sellerTransactions

    private val _singleTransaction = MutableLiveData<TransactionModel?>()
    val singleTransaction: LiveData<TransactionModel?> = _singleTransaction

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _pidx = MutableLiveData<String?>()
    val pidx: LiveData<String?> = _pidx

    fun addTransaction(transaction: TransactionModel) {
        _isLoading.value = true
        repository.addTransaction(transaction) { success, message ->
            _isLoading.value = false
            _transactionResult.value = Pair(success, message)
        }
    }

    fun fetchRenterTransactions(userId: String) {
        _isLoading.value = true
        repository.getRenterTransactions(userId) { success, data ->
            _isLoading.value = false
            if (success) {
                _renterTransactions.postValue(data)
            }
        }
    }

    fun fetchSellerTransactions(userId: String) {
        _isLoading.value = true
        repository.getSellerTransactions(userId) { success, data ->
            _isLoading.value = false
            if (success) {
                _sellerTransactions.postValue(data)
            }
        }
    }

    fun getTransactionById(transactionId: String) {
        _isLoading.value = true
        repository.getTransactionById(transactionId) { success, transaction ->
            _isLoading.value = false
            if (success) {
                _singleTransaction.postValue(transaction)
            }
        }
    }

    fun updateTransaction(transactionId: String, updates: Map<String, Any>) {
        _isLoading.value = true
        repository.updateTransaction(transactionId, updates) { success, message ->
            _isLoading.value = false
            _transactionResult.value = Pair(success, message)
        }
    }
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