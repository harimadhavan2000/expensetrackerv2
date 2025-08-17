package com.smsexpensetracker

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CapturedSmsDao {
    
    @Query("SELECT * FROM captured_sms ORDER BY timestamp DESC")
    fun getAllCapturedSms(): LiveData<List<CapturedSms>>
    
    @Query("SELECT * FROM captured_sms WHERE isFromBank = 1 ORDER BY timestamp DESC")
    fun getBankSmsOnly(): LiveData<List<CapturedSms>>
    
    @Query("SELECT * FROM captured_sms WHERE isParsed = 0 AND isFromBank = 1 ORDER BY timestamp DESC")
    fun getUnparsedBankSms(): LiveData<List<CapturedSms>>
    
    @Query("SELECT * FROM captured_sms WHERE id = :id")
    suspend fun getSmsById(id: Long): CapturedSms?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSms(sms: CapturedSms): Long
    
    @Update
    suspend fun updateSms(sms: CapturedSms)
    
    @Delete
    suspend fun deleteSms(sms: CapturedSms)
    
    @Query("DELETE FROM captured_sms")
    suspend fun deleteAllSms()
    
    @Query("DELETE FROM captured_sms WHERE timestamp < :olderThan")
    suspend fun deleteOldSms(olderThan: Long)
    
    // Mark SMS as parsed with result
    @Query("UPDATE captured_sms SET isParsed = 1, transactionId = :transactionId WHERE id = :smsId")
    suspend fun markAsParsedSuccess(smsId: Long, transactionId: Long)
    
    @Query("UPDATE captured_sms SET isParsed = 1, parsingError = :error WHERE id = :smsId")
    suspend fun markAsParsedError(smsId: Long, error: String)
}