package com.smsexpensetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TransactionsFragment : Fragment() {
    
    private lateinit var viewModel: TransactionsViewModel
    private lateinit var transactionsAdapter: TransactionsAdapter
    private lateinit var recyclerView: RecyclerView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]
        
        setupRecyclerView(view)
        observeTransactions()
    }
    
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewTransactions)
        transactionsAdapter = TransactionsAdapter { transaction ->
            showCategoryDialog(transaction)
        }
        
        recyclerView.apply {
            adapter = transactionsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun observeTransactions() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionsAdapter.submitList(transactions)
        }
    }
    
    private fun showCategoryDialog(transaction: TransactionData) {
        val categories = TransactionData.getDefaultCategories()
        val currentCategoryIndex = categories.indexOf(transaction.category).takeIf { it >= 0 } ?: 0
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Category")
            .setSingleChoiceItems(
                categories.toTypedArray(),
                currentCategoryIndex
            ) { dialog, which ->
                val selectedCategory = categories[which]
                viewModel.updateTransactionCategory(transaction, selectedCategory)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}