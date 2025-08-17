package com.smsexpensetracker

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class SmsParser(private val context: Context) {
    
    private var llmInference: LlmInference? = null
    
    init {
        try {
            // Load the Gemma3 model using MediaPipe LLM Inference
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath("gemma3-270m-it-q8.task")
                .setMaxTokens(256)  // Shorter for structured output
                .setTemperature(0.1f)  // Low temperature for deterministic output
                .setRandomSeed(42)
                .build()
                
            llmInference = LlmInference.createFromOptions(context, options)
            Log.d("SmsParser", "MediaPipe LLM model loaded successfully")
        } catch (e: Exception) {
            Log.e("SmsParser", "Error loading MediaPipe LLM model: ${e.message}")
            // Model loading failed, will rely on regex parsing as fallback
        }
    }
    
    fun parseBankSms(messageBody: String, timestamp: Long, bankName: String): TransactionData? {
        try {
            Log.d("SmsParser", "Parsing SMS with LLM first, then regex fallback")
            
            // First, try LLM-based parsing for intelligent extraction
            val llmResult = parseWithLLM(messageBody, timestamp, bankName)
            if (llmResult != null) {
                Log.d("SmsParser", "LLM parsing successful")
                return llmResult
            }
            
            // If LLM fails, fall back to regex parsing
            Log.d("SmsParser", "LLM parsing failed, trying regex fallback")
            val regexResult = parseWithRegex(messageBody, timestamp, bankName)
            if (regexResult != null) {
                Log.d("SmsParser", "Regex parsing successful")
                return regexResult
            }
            
            Log.d("SmsParser", "Both LLM and regex parsing failed")
            return null
            
        } catch (e: Exception) {
            Log.e("SmsParser", "Error parsing SMS: ${e.message}")
            return null
        }
    }
    
    private fun parseWithRegex(messageBody: String, timestamp: Long, bankName: String): TransactionData? {
        Log.d("SmsParser", "Parsing SMS: $messageBody")
        
        // Multiple patterns to handle different SMS formats
        val amountPatterns = listOf(
            Pattern.compile("(?:Rs\\.?|INR|₹)\\s*([0-9,]+\\.?[0-9]*)"),
            Pattern.compile("([0-9,]+\\.?[0-9]*)\\s*(?:Rs\\.?|INR|₹)"),
            Pattern.compile("amount[\\s:]*(?:Rs\\.?|INR|₹)?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("sent\\s+(?:Rs\\.?|INR|₹)?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE)
        )
        
        val accountPatterns = listOf(
            Pattern.compile("A/C[\\s*]+([0-9X*]+)"),
            Pattern.compile("from[\\s]+[A-Z\\s]+A/C[\\s*]+([0-9X*]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("account[\\s*]+([0-9X*]+)", Pattern.CASE_INSENSITIVE)
        )
        
        val refPatterns = listOf(
            Pattern.compile("(?:Ref|UPI Ref|Txn|Transaction|Reference)[\\s:]+([A-Z0-9]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("UTR[\\s:]*([A-Z0-9]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("TXN[\\s:]*([A-Z0-9]+)", Pattern.CASE_INSENSITIVE)
        )
        
        val merchantPatterns = listOf(
            // Prioritize "To" over "From" - put "To" patterns first
            Pattern.compile("To\\s+([A-Z0-9\\s]+?)(?:\\s+On|\\s+UPI|\\s+A/C|\\s*$)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:paid to|sent to)\\s+([A-Z0-9\\s]+?)(?:\\s+on|\\s+via|\\s+from)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("merchant[\\s:]+([A-Z0-9\\s]+)", Pattern.CASE_INSENSITIVE),
            // "From" patterns last as fallback
            Pattern.compile("FROM\\s+([A-Z0-9\\s]+?)(?:\\s+On|\\s+UPI|\\s+A/C)", Pattern.CASE_INSENSITIVE)
        )
        
        var amount: Double? = null
        var accountNumber: String? = null
        var referenceNumber: String? = null
        var merchant: String? = null
        
        // Try multiple amount patterns
        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(messageBody)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "")
                amount = amountStr?.toDoubleOrNull()
                if (amount != null) {
                    Log.d("SmsParser", "Amount found: $amount")
                    break
                }
            }
        }
        
        // Try multiple account patterns
        for (pattern in accountPatterns) {
            val matcher = pattern.matcher(messageBody)
            if (matcher.find()) {
                accountNumber = matcher.group(1)
                if (accountNumber != null) {
                    Log.d("SmsParser", "Account found: $accountNumber")
                    break
                }
            }
        }
        
        // Try multiple reference patterns
        for (pattern in refPatterns) {
            val matcher = pattern.matcher(messageBody)
            if (matcher.find()) {
                referenceNumber = matcher.group(1)
                if (referenceNumber != null) {
                    Log.d("SmsParser", "Reference found: $referenceNumber")
                    break
                }
            }
        }
        
        // Try multiple merchant patterns
        for (pattern in merchantPatterns) {
            val matcher = pattern.matcher(messageBody)
            if (matcher.find()) {
                merchant = matcher.group(1)?.trim()
                if (merchant != null && merchant.isNotBlank()) {
                    Log.d("SmsParser", "Merchant found: $merchant")
                    break
                }
            }
        }
        
        // If no merchant found, try to extract any capitalized words as fallback
        if (merchant.isNullOrBlank()) {
            val fallbackPattern = Pattern.compile("(?:TO|FROM)\\s+([A-Z][A-Z0-9\\s]{2,})", Pattern.CASE_INSENSITIVE)
            val fallbackMatcher = fallbackPattern.matcher(messageBody)
            if (fallbackMatcher.find()) {
                merchant = fallbackMatcher.group(1)?.trim()
                Log.d("SmsParser", "Fallback merchant found: $merchant")
            }
        }
        
        return if (amount != null && merchant != null) {
            TransactionData(
                id = 0,
                amount = amount,
                currency = "INR",
                merchant = merchant,
                accountNumber = accountNumber ?: "",
                referenceNumber = referenceNumber ?: "",
                timestamp = timestamp,
                date = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(timestamp)),
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp)),
                bankName = bankName,
                category = "", // Will be filled by user
                rawSmsText = messageBody
            )
        } else null
    }
    
    private fun parseWithLLM(messageBody: String, timestamp: Long, bankName: String): TransactionData? {
        return llmInference?.let { llm ->
            try {
                // Create a structured prompt for the Gemma3 model
                val prompt = """
Extract transaction information from this bank SMS and respond ONLY with a JSON object:

SMS: "$messageBody"

Extract these fields:
- amount: numeric value only (no currency symbols)
- merchant: recipient/merchant name  
- reference: transaction reference/ID
- account: last 4 digits of account

Respond with ONLY this JSON format:
{"amount":"[number]","merchant":"[name]","reference":"[ref]","account":"[digits]"}

JSON:""".trimIndent()
                
                Log.d("SmsParser", "Sending prompt to LLM: $prompt")
                
                // Generate response using MediaPipe LLM Inference
                val response = llm.generateResponse(prompt)
                Log.d("SmsParser", "LLM Response: $response")
                
                // Parse the JSON response to extract transaction data
                return parseJsonResponse(response, timestamp, bankName, messageBody)
                
            } catch (e: Exception) {
                Log.e("SmsParser", "Error with MediaPipe LLM parsing: ${e.message}")
                null
            }
        }
    }
    
    private fun parseJsonResponse(jsonResponse: String, timestamp: Long, bankName: String, originalSms: String): TransactionData? {
        return try {
            Log.d("SmsParser", "Parsing JSON response: $jsonResponse")
            
            // Clean the response - sometimes LLM adds extra text
            val jsonStart = jsonResponse.indexOf("{")
            val jsonEnd = jsonResponse.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                Log.e("SmsParser", "No valid JSON found in response")
                return null
            }
            
            val jsonString = jsonResponse.substring(jsonStart, jsonEnd)
            Log.d("SmsParser", "Cleaned JSON: $jsonString")
            
            // Simple JSON parsing using regex (more robust than manual parsing)
            val amountRegex = "\"amount\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val merchantRegex = "\"merchant\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val referenceRegex = "\"reference\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val accountRegex = "\"account\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            
            val amountMatch = amountRegex.find(jsonString)
            val merchantMatch = merchantRegex.find(jsonString)
            val referenceMatch = referenceRegex.find(jsonString)
            val accountMatch = accountRegex.find(jsonString)
            
            val amountStr = amountMatch?.groupValues?.get(1)?.replace(",", "")
            val amount = amountStr?.toDoubleOrNull()
            val merchant = merchantMatch?.groupValues?.get(1)?.trim()
            val reference = referenceMatch?.groupValues?.get(1)?.trim()
            val account = accountMatch?.groupValues?.get(1)?.trim()
            
            Log.d("SmsParser", "Extracted - Amount: $amount, Merchant: $merchant, Ref: $reference, Account: $account")
            
            if (amount != null && !merchant.isNullOrBlank() && merchant != "null") {
                TransactionData(
                    id = 0,
                    amount = amount,
                    currency = "INR",
                    merchant = merchant,
                    accountNumber = account ?: "",
                    referenceNumber = reference ?: "",
                    timestamp = timestamp,
                    date = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(timestamp)),
                    time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp)),
                    bankName = bankName,
                    category = "",
                    rawSmsText = originalSms
                )
            } else {
                Log.e("SmsParser", "Invalid extracted data - Amount: $amount, Merchant: '$merchant'")
                null
            }
            
        } catch (e: Exception) {
            Log.e("SmsParser", "Error parsing JSON response: ${e.message}")
            null
        }
    }
    
    private fun extractAmountFromText(text: String): String? {
        val amountPattern = Pattern.compile("([0-9,]+\\.?[0-9]*)")
        val matcher = amountPattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.replace(",", "")
        } else null
    }
    
    fun cleanup() {
        llmInference?.close()
    }
}