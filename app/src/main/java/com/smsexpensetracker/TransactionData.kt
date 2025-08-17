package com.smsexpensetracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "transactions")
data class TransactionData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val currency: String,
    val merchant: String,
    val accountNumber: String,
    val referenceNumber: String,
    val timestamp: Long,
    val date: String,
    val time: String,
    val bankName: String,
    val category: String,
    val rawSmsText: String,
    val isExpense: Boolean = true, // true for debit, false for credit
    val notes: String = ""
) {
    companion object {
        const val CATEGORY_FOOD = "Food & Dining"
        const val CATEGORY_TRANSPORT = "Transport"
        const val CATEGORY_SHOPPING = "Shopping"
        const val CATEGORY_ENTERTAINMENT = "Entertainment"
        const val CATEGORY_BILLS = "Bills & Utilities"
        const val CATEGORY_HEALTHCARE = "Healthcare"
        const val CATEGORY_EDUCATION = "Education"
        const val CATEGORY_TRAVEL = "Travel"
        const val CATEGORY_GROCERIES = "Groceries"
        const val CATEGORY_FUEL = "Fuel"
        const val CATEGORY_OTHER = "Other"
        
        fun getDefaultCategories(): List<String> {
            return listOf(
                CATEGORY_FOOD,
                CATEGORY_TRANSPORT,
                CATEGORY_SHOPPING,
                CATEGORY_ENTERTAINMENT,
                CATEGORY_BILLS,
                CATEGORY_HEALTHCARE,
                CATEGORY_EDUCATION,
                CATEGORY_TRAVEL,
                CATEGORY_GROCERIES,
                CATEGORY_FUEL,
                CATEGORY_OTHER
            )
        }
    }
    
    fun getFormattedAmount(): String {
        return "₹${String.format("%.2f", amount)}"
    }
    
    fun getFormattedDateTime(): String {
        return "$date $time"
    }
    
    fun getMonthYear(): String {
        return try {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)
        } catch (e: Exception) {
            ""
        }
    }
    
    fun getDayOfMonth(): Int {
        return try {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            calendar.get(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) {
            0
        }
    }
}