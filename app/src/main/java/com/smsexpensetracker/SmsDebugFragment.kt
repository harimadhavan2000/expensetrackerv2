package com.smsexpensetracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SmsDebugFragment : Fragment() {
    
    private lateinit var viewModel: SmsDebugViewModel
    private lateinit var smsDebugAdapter: SmsDebugAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var autoParseSwitch: Switch
    private lateinit var statsText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sms_debug, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[SmsDebugViewModel::class.java]
        
        setupViews(view)
        setupRecyclerView()
        observeData()
        loadSettings()
    }
    
    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewSmsDebug)
        autoParseSwitch = view.findViewById(R.id.switchAutoParse)
        statsText = view.findViewById(R.id.textSmsStats)
        
        autoParseSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveAutoParseSettings(isChecked)
            Toast.makeText(context, 
                if (isChecked) "Auto-parsing enabled" else "Auto-parsing disabled", 
                Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupRecyclerView() {
        smsDebugAdapter = SmsDebugAdapter(
            onParseClick = { sms ->
                parseSmsManually(sms)
            },
            onDeleteClick = { sms ->
                viewModel.deleteSms(sms)
                Toast.makeText(context, "SMS deleted", Toast.LENGTH_SHORT).show()
            }
        )
        
        recyclerView.apply {
            adapter = smsDebugAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun observeData() {
        viewModel.allCapturedSms.observe(viewLifecycleOwner) { smsList ->
            smsDebugAdapter.submitList(smsList)
            updateStats(smsList)
        }
    }
    
    private fun updateStats(smsList: List<CapturedSms>) {
        val totalCount = smsList.size
        val bankSmsCount = smsList.count { it.isFromBank }
        val parsedCount = smsList.count { it.isParsed && it.transactionId != null }
        val errorCount = smsList.count { it.parsingError != null }
        
        statsText.text = "Total: $totalCount | Bank SMS: $bankSmsCount | Parsed: $parsedCount | Errors: $errorCount"
    }
    
    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        autoParseSwitch.isChecked = prefs.getBoolean("auto_parse_sms", false)
    }
    
    private fun saveAutoParseSettings(enabled: Boolean) {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("auto_parse_sms", enabled).apply()
    }
    
    private fun parseSmsManually(sms: CapturedSms) {
        if (!sms.isFromBank) {
            Toast.makeText(context, "Can only parse bank SMS", Toast.LENGTH_SHORT).show()
            return
        }
        
        Toast.makeText(context, "Parsing SMS...", Toast.LENGTH_SHORT).show()
        
        viewModel.parseSmsManually(sms) { success, error, transactionId ->
            requireActivity().runOnUiThread {
                if (success) {
                    Toast.makeText(context, "✅ SMS parsed successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "❌ Parsing failed: $error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}