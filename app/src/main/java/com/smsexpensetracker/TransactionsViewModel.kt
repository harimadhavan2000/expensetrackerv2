package com.smsexpensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = TransactionDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    
    val allTransactions: LiveData<List<TransactionData>> = transactionDao.getAllTransactions()
    
    fun updateTransactionCategory(transaction: TransactionData, category: String) {
        viewModelScope.launch {
            val updatedTransaction = transaction.copy(category = category)
            transactionDao.updateTransaction(updatedTransaction)
        }
    }
    
    fun deleteTransaction(transaction: TransactionData) {
        viewModelScope.launch {
            transactionDao.deleteTransaction(transaction)
        }
    }
    
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): LiveData<List<TransactionData>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }
    
    fun getTransactionsByCategory(category: String): LiveData<List<TransactionData>> {
        return transactionDao.getTransactionsByCategory(category)
    }
}