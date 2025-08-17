package com.smsexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class BankIdentifiersAdapter(
    private val onEditClick: (BankIdentifier) -> Unit,
    private val onDeleteClick: (BankIdentifier) -> Unit
) : ListAdapter<BankIdentifier, BankIdentifiersAdapter.BankIdentifierViewHolder>(BankIdentifierDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankIdentifierViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bank_identifier, parent, false)
        return BankIdentifierViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: BankIdentifierViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class BankIdentifierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textBankName: TextView = itemView.findViewById(R.id.textBankName)
        private val textIdentifier: TextView = itemView.findViewById(R.id.textIdentifier)
        private val buttonEdit: Button = itemView.findViewById(R.id.buttonEdit)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)
        
        fun bind(bankIdentifier: BankIdentifier) {
            textBankName.text = bankIdentifier.bankName
            textIdentifier.text = bankIdentifier.identifier
            
            buttonEdit.setOnClickListener {
                onEditClick(bankIdentifier)
            }
            
            buttonDelete.setOnClickListener {
                onDeleteClick(bankIdentifier)
            }
        }
    }
    
    private class BankIdentifierDiffCallback : DiffUtil.ItemCallback<BankIdentifier>() {
        override fun areItemsTheSame(oldItem: BankIdentifier, newItem: BankIdentifier): Boolean {
            return oldItem.identifier == newItem.identifier
        }
        
        override fun areContentsTheSame(oldItem: BankIdentifier, newItem: BankIdentifier): Boolean {
            return oldItem == newItem
        }
    }
}