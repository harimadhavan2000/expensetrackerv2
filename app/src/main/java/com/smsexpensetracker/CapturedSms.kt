package com.smsexpensetracker

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "captured_sms")
data class CapturedSms(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val messageBody: String,
    val timestamp: Long,
    val isFromBank: Boolean,
    val bankName: String?,
    val isParsed: Boolean = false,
    val parsingError: String? = null,
    val transactionId: Long? = null // Link to created transaction if parsed successfully
) {
    fun getFormattedDateTime(): String {
        return SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
    
    fun getFormattedSender(): String {
        return if (isFromBank && bankName != null) {
            "$sender ($bankName)"
        } else {
            sender
        }
    }
    
    fun getStatusText(): String {
        return when {
            !isFromBank -> "Not Bank SMS"
            isParsed && transactionId != null -> "✅ Parsed"
            isParsed && transactionId == null -> "❌ Parse Failed"
            parsingError != null -> "❌ Error"
            else -> "⏸️ Not Parsed"
        }
    }
}