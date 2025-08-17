package com.smsexpensetracker

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class BankIdentifier(
    val identifier: String,
    val bankName: String,
    val isActive: Boolean = true
)

object BankIdentifierManager {
    private const val PREFS_NAME = "bank_identifiers"
    private const val KEY_IDENTIFIERS = "identifiers"
    
    private val defaultBankIdentifiers = listOf(
        BankIdentifier("VM-HDFCBK", "HDFC Bank"),
        BankIdentifier("VK-HDFCBK", "HDFC Bank"),
        BankIdentifier("AD-HDFCBK", "HDFC Bank"),
        BankIdentifier("VM-ICICIB", "ICICI Bank"),
        BankIdentifier("VK-ICICIB", "ICICI Bank"),
        BankIdentifier("VM-SBIBNK", "State Bank of India"),
        BankIdentifier("VK-SBIBNK", "State Bank of India"),
        BankIdentifier("VM-AXISB", "Axis Bank"),
        BankIdentifier("VK-AXIBNK", "Axis Bank"),
        BankIdentifier("VM-KOTAKB", "Kotak Bank"),
        BankIdentifier("VK-KOTAKB", "Kotak Bank"),
        BankIdentifier("VM-PAYTM", "Paytm Payments Bank"),
        BankIdentifier("VK-PYTMB", "Paytm Payments Bank"),
        BankIdentifier("VM-IDFCFB", "IDFC First Bank"),
        BankIdentifier("VK-IDFCFB", "IDFC First Bank")
    )
    
    fun getBankIdentifiers(context: Context): List<BankIdentifier> {
        val prefs = getPreferences(context)
        val json = prefs.getString(KEY_IDENTIFIERS, null)
        
        return if (json != null) {
            try {
                val type = object : TypeToken<List<BankIdentifier>>() {}.type
                Gson().fromJson(json, type)
            } catch (e: Exception) {
                // If parsing fails, return default identifiers
                defaultBankIdentifiers
            }
        } else {
            // First time, save default identifiers
            saveBankIdentifiers(context, defaultBankIdentifiers)
            defaultBankIdentifiers
        }
    }
    
    fun saveBankIdentifiers(context: Context, identifiers: List<BankIdentifier>) {
        val prefs = getPreferences(context)
        val json = Gson().toJson(identifiers)
        prefs.edit().putString(KEY_IDENTIFIERS, json).apply()
    }
    
    fun addBankIdentifier(context: Context, identifier: BankIdentifier) {
        val currentIdentifiers = getBankIdentifiers(context).toMutableList()
        currentIdentifiers.add(identifier)
        saveBankIdentifiers(context, currentIdentifiers)
    }
    
    fun removeBankIdentifier(context: Context, identifier: String) {
        val currentIdentifiers = getBankIdentifiers(context).toMutableList()
        currentIdentifiers.removeAll { it.identifier == identifier }
        saveBankIdentifiers(context, currentIdentifiers)
    }
    
    fun updateBankIdentifier(context: Context, oldIdentifier: String, newIdentifier: BankIdentifier) {
        val currentIdentifiers = getBankIdentifiers(context).toMutableList()
        val index = currentIdentifiers.indexOfFirst { it.identifier == oldIdentifier }
        if (index != -1) {
            currentIdentifiers[index] = newIdentifier
            saveBankIdentifiers(context, currentIdentifiers)
        }
    }
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}