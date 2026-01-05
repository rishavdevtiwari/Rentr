package com.example.rentr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rentr.model.TransactionModel
import com.example.rentr.repository.TransactionRepo

class TransactionViewModel(private val repository: TransactionRepo) : ViewModel() {

    private val _transactionResult = MutableLiveData<Pair<Boolean, String?>>()
    val transactionResult: LiveData<Pair<Boolean, String?>> = _transactionResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun addTransaction(transaction: TransactionModel) {
        _isLoading.value = true
        repository.addTransaction(transaction) { success, message ->
            _isLoading.value = false
            _transactionResult.value = Pair(success, message)
        }
    }
}
