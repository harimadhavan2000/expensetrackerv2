package com.smsexpensetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "sms_receiver_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("SmsReceiver", "BroadcastReceiver triggered with action: ${intent?.action}")
        
        // Show notification to confirm receiver is triggered
        context?.let {
            createNotificationChannel(it)
            showNotification(it, "SMS Received", "SMS Expense Tracker is monitoring messages")
        }
        
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d("SmsReceiver", "SMS_RECEIVED_ACTION confirmed")
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            for (smsMessage in messages) {
                val messageBody = smsMessage.messageBody
                val sender = smsMessage.originatingAddress
                val timestamp = smsMessage.timestampMillis
                
                Log.d("SmsReceiver", "SMS received from: $sender")
                Log.d("SmsReceiver", "Message: $messageBody")
                
                context?.let {
                    Log.d("SmsReceiver", "Context available, storing SMS...")
                    
                    // Show detailed notification for captured SMS
                    showNotification(it, "SMS Captured", "From: ${sender ?: "Unknown"}\n${messageBody.take(50)}...")
                    
                    // Store all SMS messages for debugging
                    storeCapturedSms(it, messageBody, sender ?: "", timestamp)
                    
                    // Process bank SMS if auto-parsing is enabled
                    if (shouldAutoProcess(it)) {
                        Log.d("SmsReceiver", "Auto-processing enabled, processing SMS...")
                        processBankSms(it, messageBody, sender ?: "", timestamp)
                    } else {
                        Log.d("SmsReceiver", "Auto-processing disabled, SMS stored for manual processing")
                    }
                }
            }
        }
    }
    
    private fun processBankSms(context: Context, messageBody: String, sender: String, timestamp: Long) {
        val bankIdentifiers = BankIdentifierManager.getBankIdentifiers(context)
        
        // Check if this SMS is from a configured bank
        val bankInfo = bankIdentifiers.find { bankId ->
            sender.contains(bankId.identifier, ignoreCase = true)
        }
        
        if (bankInfo != null) {
            Log.d("SmsReceiver", "Bank SMS detected: ${bankInfo.bankName}")
            
            // Debug toast for bank SMS detection
            CoroutineScope(Dispatchers.Main).launch {
                android.widget.Toast.makeText(context, "Bank SMS: ${bankInfo.bankName}", android.widget.Toast.LENGTH_LONG).show()
            }
            
            // Use coroutine to process SMS parsing in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val smsParser = SmsParser(context)
                    val transactionData = smsParser.parseBankSms(messageBody, timestamp, bankInfo.bankName)
                    
                    if (transactionData != null) {
                        // Save to database
                        val database = TransactionDatabase.getDatabase(context)
                        database.transactionDao().insertTransaction(transactionData)
                        
                        Log.d("SmsReceiver", "Transaction saved: ${transactionData.amount}")
                        
                        // Debug toast for successful save
                        CoroutineScope(Dispatchers.Main).launch {
                            android.widget.Toast.makeText(context, "Transaction saved: ₹${transactionData.amount}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.d("SmsReceiver", "SMS parsing returned null")
                        CoroutineScope(Dispatchers.Main).launch {
                            android.widget.Toast.makeText(context, "SMS parsing failed", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SmsReceiver", "Error parsing SMS: ${e.message}")
                    CoroutineScope(Dispatchers.Main).launch {
                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Log.d("SmsReceiver", "SMS not from configured bank. Sender: $sender")
        }
    }
    
    private fun storeCapturedSms(context: Context, messageBody: String, sender: String, timestamp: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bankIdentifiers = BankIdentifierManager.getBankIdentifiers(context)
                val bankInfo = bankIdentifiers.find { bankId ->
                    sender.contains(bankId.identifier, ignoreCase = true)
                }
                
                val capturedSms = CapturedSms(
                    sender = sender,
                    messageBody = messageBody,
                    timestamp = timestamp,
                    isFromBank = bankInfo != null,
                    bankName = bankInfo?.bankName
                )
                
                val database = TransactionDatabase.getDatabase(context)
                val insertedId = database.capturedSmsDao().insertSms(capturedSms)
                
                Log.d("SmsReceiver", "SMS stored for debugging with ID: $insertedId")
                Log.d("SmsReceiver", "SMS details - Sender: ${capturedSms.sender}, Bank: ${capturedSms.isFromBank}, BankName: ${capturedSms.bankName}")
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error storing captured SMS: ${e.message}")
            }
        }
    }
    
    private fun shouldAutoProcess(context: Context): Boolean {
        // Check if auto-processing is enabled (default: false for debugging)
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("auto_parse_sms", false)
    }
    
    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "SMS Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when SMS messages are captured by the expense tracker"
                setShowBadge(false)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(context: Context, title: String, content: String) {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}