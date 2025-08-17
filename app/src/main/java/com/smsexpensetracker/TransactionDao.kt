package com.smsexpensetracker

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): LiveData<List<TransactionData>>
    
    @Query("SELECT * FROM transactions WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): LiveData<List<TransactionData>>
    
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getTransactionsByCategory(category: String): LiveData<List<TransactionData>>
    
    @Query("SELECT DISTINCT category FROM transactions WHERE category != '' ORDER BY category")
    fun getAllCategories(): LiveData<List<String>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startDate AND timestamp <= :endDate")
    suspend fun getTotalAmountForPeriod(startDate: Long, endDate: Long): Double?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE category = :category AND timestamp >= :startDate AND timestamp <= :endDate")
    suspend fun getTotalAmountForCategoryAndPeriod(category: String, startDate: Long, endDate: Long): Double?
    
    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE timestamp >= :startDate AND timestamp <= :endDate AND category != '' GROUP BY category ORDER BY total DESC")
    suspend fun getCategoryTotalsForPeriod(startDate: Long, endDate: Long): List<CategoryTotal>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionData?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionData): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionData>)
    
    @Update
    suspend fun updateTransaction(transaction: TransactionData)
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionData)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
    
    // Monthly report queries
    @Query("SELECT strftime('%Y-%m', datetime(timestamp/1000, 'unixepoch')) as month, SUM(amount) as total FROM transactions GROUP BY month ORDER BY month DESC")
    suspend fun getMonthlyTotals(): List<MonthlyTotal>
    
    // Daily report queries  
    @Query("SELECT DATE(timestamp/1000, 'unixepoch') as date, SUM(amount) as total FROM transactions WHERE timestamp >= :startDate AND timestamp <= :endDate GROUP BY date ORDER BY date DESC")
    suspend fun getDailyTotalsForPeriod(startDate: Long, endDate: Long): List<DailyTotal>
}

data class CategoryTotal(
    val category: String,
    val total: Double
)

data class MonthlyTotal(
    val month: String,
    val total: Double
)

data class DailyTotal(
    val date: String,
    val total: Double
)