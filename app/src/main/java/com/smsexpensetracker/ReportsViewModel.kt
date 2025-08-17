package com.smsexpensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = TransactionDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    
    private val _categoryTotals = MutableLiveData<List<CategoryTotal>>()
    val categoryTotals: LiveData<List<CategoryTotal>> = _categoryTotals
    
    private val _totalAmount = MutableLiveData<Double>()
    val totalAmount: LiveData<Double> = _totalAmount
    
    fun loadDataForPeriod(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            try {
                val categories = transactionDao.getCategoryTotalsForPeriod(startDate, endDate)
                val total = transactionDao.getTotalAmountForPeriod(startDate, endDate) ?: 0.0
                
                _categoryTotals.postValue(categories)
                _totalAmount.postValue(total)
            } catch (e: Exception) {
                _categoryTotals.postValue(emptyList())
                _totalAmount.postValue(0.0)
            }
        }
    }
}