package com.smsexpensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsDebugViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = TransactionDatabase.getDatabase(application)
    private val capturedSmsDao = database.capturedSmsDao()
    private val transactionDao = database.transactionDao()
    
    val allCapturedSms: LiveData<List<CapturedSms>> = capturedSmsDao.getAllCapturedSms()
    val bankSmsOnly: LiveData<List<CapturedSms>> = capturedSmsDao.getBankSmsOnly()
    val unparsedBankSms: LiveData<List<CapturedSms>> = capturedSmsDao.getUnparsedBankSms()
    
    fun parseSmsManually(sms: CapturedSms, callback: (success: Boolean, error: String?, transactionId: Long?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val smsParser = SmsParser(getApplication())
                val transactionData = smsParser.parseBankSms(sms.messageBody, sms.timestamp, sms.bankName ?: "Unknown")
                
                if (transactionData != null) {
                    // Save transaction to database
                    val transactionId = transactionDao.insertTransaction(transactionData)
                    
                    // Mark SMS as successfully parsed
                    capturedSmsDao.markAsParsedSuccess(sms.id, transactionId)
                    
                    callback(true, null, transactionId)
                } else {
                    // Mark SMS as failed to parse
                    capturedSmsDao.markAsParsedError(sms.id, "Failed to extract required fields (amount/merchant)")
                    
                    callback(false, "Failed to extract required fields (amount/merchant)", null)
                }
            } catch (e: Exception) {
                // Mark SMS as error
                val errorMessage = "Error: ${e.message}"
                capturedSmsDao.markAsParsedError(sms.id, errorMessage)
                
                callback(false, errorMessage, null)
            }
        }
    }
    
    fun deleteSms(sms: CapturedSms) {
        viewModelScope.launch(Dispatchers.IO) {
            capturedSmsDao.deleteSms(sms)
        }
    }
    
    fun deleteAllSms() {
        viewModelScope.launch(Dispatchers.IO) {
            capturedSmsDao.deleteAllSms()
        }
    }
    
    fun deleteOldSms(daysOld: Int = 30) {
        viewModelScope.launch(Dispatchers.IO) {
            val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
            capturedSmsDao.deleteOldSms(cutoffTime)
        }
    }
}