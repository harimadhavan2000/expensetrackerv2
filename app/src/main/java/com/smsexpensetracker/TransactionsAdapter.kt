package com.smsexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TransactionsAdapter(
    private val onCategoryClick: (TransactionData) -> Unit
) : ListAdapter<TransactionData, TransactionsAdapter.TransactionViewHolder>(TransactionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMerchant: TextView = itemView.findViewById(R.id.textMerchant)
        private val textAmount: TextView = itemView.findViewById(R.id.textAmount)
        private val textDateTime: TextView = itemView.findViewById(R.id.textDateTime)
        private val textCategory: TextView = itemView.findViewById(R.id.textCategory)
        private val textBank: TextView = itemView.findViewById(R.id.textBank)
        
        fun bind(transaction: TransactionData) {
            textMerchant.text = transaction.merchant
            textAmount.text = transaction.getFormattedAmount()
            textDateTime.text = transaction.getFormattedDateTime()
            textBank.text = transaction.bankName
            
            if (transaction.category.isNotEmpty()) {
                textCategory.text = transaction.category
                textCategory.visibility = View.VISIBLE
            } else {
                textCategory.text = "Tap to categorize"
                textCategory.visibility = View.VISIBLE
            }
            
            itemView.setOnClickListener {
                onCategoryClick(transaction)
            }
        }
    }
    
    private class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionData>() {
        override fun areItemsTheSame(oldItem: TransactionData, newItem: TransactionData): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: TransactionData, newItem: TransactionData): Boolean {
            return oldItem == newItem
        }
    }
}