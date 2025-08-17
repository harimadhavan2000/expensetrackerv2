package com.smsexpensetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsFragment : Fragment() {
    
    private lateinit var recyclerViewBanks: RecyclerView
    private lateinit var buttonAddBank: Button
    private lateinit var bankIdentifiersAdapter: BankIdentifiersAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupRecyclerView()
        loadBankIdentifiers()
    }
    
    private fun initializeViews(view: View) {
        recyclerViewBanks = view.findViewById(R.id.recyclerViewBanks)
        buttonAddBank = view.findViewById(R.id.buttonAddBank)
        
        buttonAddBank.setOnClickListener {
            showAddBankDialog()
        }
    }
    
    private fun setupRecyclerView() {
        bankIdentifiersAdapter = BankIdentifiersAdapter(
            onEditClick = { bankIdentifier ->
                showEditBankDialog(bankIdentifier)
            },
            onDeleteClick = { bankIdentifier ->
                showDeleteBankDialog(bankIdentifier)
            }
        )
        
        recyclerViewBanks.apply {
            adapter = bankIdentifiersAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun loadBankIdentifiers() {
        val bankIdentifiers = BankIdentifierManager.getBankIdentifiers(requireContext())
        bankIdentifiersAdapter.submitList(bankIdentifiers)
    }
    
    private fun showAddBankDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_bank, null)
        val editIdentifier = dialogView.findViewById<EditText>(R.id.editIdentifier)
        val editBankName = dialogView.findViewById<EditText>(R.id.editBankName)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add Bank Identifier")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val identifier = editIdentifier.text.toString().trim()
                val bankName = editBankName.text.toString().trim()
                
                if (identifier.isNotEmpty() && bankName.isNotEmpty()) {
                    val bankIdentifier = BankIdentifier(identifier, bankName, true)
                    BankIdentifierManager.addBankIdentifier(requireContext(), bankIdentifier)
                    loadBankIdentifiers()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditBankDialog(bankIdentifier: BankIdentifier) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_bank, null)
        val editIdentifier = dialogView.findViewById<EditText>(R.id.editIdentifier)
        val editBankName = dialogView.findViewById<EditText>(R.id.editBankName)
        
        editIdentifier.setText(bankIdentifier.identifier)
        editBankName.setText(bankIdentifier.bankName)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Bank Identifier")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val identifier = editIdentifier.text.toString().trim()
                val bankName = editBankName.text.toString().trim()
                
                if (identifier.isNotEmpty() && bankName.isNotEmpty()) {
                    val updatedBankIdentifier = BankIdentifier(identifier, bankName, bankIdentifier.isActive)
                    BankIdentifierManager.updateBankIdentifier(requireContext(), bankIdentifier.identifier, updatedBankIdentifier)
                    loadBankIdentifiers()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteBankDialog(bankIdentifier: BankIdentifier) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Bank Identifier")
            .setMessage("Are you sure you want to delete ${bankIdentifier.bankName} (${bankIdentifier.identifier})?")
            .setPositiveButton("Delete") { _, _ ->
                BankIdentifierManager.removeBankIdentifier(requireContext(), bankIdentifier.identifier)
                loadBankIdentifiers()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}