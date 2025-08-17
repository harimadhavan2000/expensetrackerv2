package com.smsexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class SmsDebugAdapter(
    private val onParseClick: (CapturedSms) -> Unit,
    private val onDeleteClick: (CapturedSms) -> Unit
) : ListAdapter<CapturedSms, SmsDebugAdapter.SmsDebugViewHolder>(SmsDebugDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsDebugViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sms_debug, parent, false)
        return SmsDebugViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SmsDebugViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class SmsDebugViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textSender: TextView = itemView.findViewById(R.id.textSender)
        private val textDateTime: TextView = itemView.findViewById(R.id.textDateTime)
        private val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        private val textStatus: TextView = itemView.findViewById(R.id.textStatus)
        private val textError: TextView = itemView.findViewById(R.id.textError)
        private val buttonParse: Button = itemView.findViewById(R.id.buttonParse)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)
        
        fun bind(sms: CapturedSms) {
            textSender.text = sms.getFormattedSender()
            textDateTime.text = sms.getFormattedDateTime()
            textMessage.text = sms.messageBody
            textStatus.text = sms.getStatusText()
            
            // Show/hide error text
            if (!sms.parsingError.isNullOrBlank()) {
                textError.visibility = View.VISIBLE
                textError.text = "Error: ${sms.parsingError}"
            } else {
                textError.visibility = View.GONE
            }
            
            // Enable/disable parse button based on conditions
            buttonParse.isEnabled = sms.isFromBank && (!sms.isParsed || sms.transactionId == null)
            buttonParse.text = if (sms.isParsed && sms.transactionId == null) "Retry Parse" else "Parse"
            
            // Set status text color
            when {
                sms.isParsed && sms.transactionId != null -> {
                    textStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                }
                sms.parsingError != null -> {
                    textStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                }
                sms.isFromBank -> {
                    textStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
                else -> {
                    textStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                }
            }
            
            buttonParse.setOnClickListener {
                onParseClick(sms)
            }
            
            buttonDelete.setOnClickListener {
                onDeleteClick(sms)
            }
        }
    }
    
    private class SmsDebugDiffCallback : DiffUtil.ItemCallback<CapturedSms>() {
        override fun areItemsTheSame(oldItem: CapturedSms, newItem: CapturedSms): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: CapturedSms, newItem: CapturedSms): Boolean {
            return oldItem == newItem
        }
    }
}