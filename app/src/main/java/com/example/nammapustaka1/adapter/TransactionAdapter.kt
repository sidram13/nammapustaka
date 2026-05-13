package com.example.nammapustaka1.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nammapustaka1.data.model.Transaction
import com.example.nammapustaka1.data.model.TransactionStatus
import com.example.nammapustaka1.databinding.ItemTransactionBinding
import com.example.nammapustaka1.utils.DateUtils

class TransactionAdapter(
    private val bookTitleMap: Map<Long, String> = emptyMap(),
    private val studentNameMap: Map<Long, String> = emptyMap(),
    private val onReturnClick: ((Transaction) -> Unit)? = null
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tx: Transaction) {
            binding.tvBookName.text = bookTitleMap[tx.bookId] ?: "ಪುಸ್ತಕ #${tx.bookId}"
            binding.tvStudentName.text = studentNameMap[tx.studentId] ?: "ವಿದ್ಯಾರ್ಥಿ #${tx.studentId}"
            binding.tvIssueDate.text = "ನೀಡಿದ ದಿನ: ${DateUtils.formatDate(tx.issueDate)}"
            binding.tvDueDate.text = "ಕೊನೆಯ ದಿನ: ${DateUtils.formatDate(tx.dueDate)}"

            // Status badge with color coding — OVERDUE turns RED
            when {
                tx.status == TransactionStatus.OVERDUE || DateUtils.isOverdue(tx.dueDate) && tx.status == TransactionStatus.ISSUED -> {
                    val daysLate = DateUtils.daysOverdue(tx.dueDate)
                    binding.tvStatus.text = "ತಡವಾಗಿದೆ - $daysLate ದಿನ"
                    binding.tvStatus.setTextColor(Color.RED)
                    binding.tvDueDate.setTextColor(Color.RED)
                }
                tx.status == TransactionStatus.RETURNED -> {
                    binding.tvStatus.text = "ಹಿಂತಿರುಗಿಸಲಾಗಿದೆ"
                    binding.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
                    binding.tvDueDate.setTextColor(Color.BLACK)
                }
                else -> {
                    val daysLeft = DateUtils.daysRemaining(tx.dueDate)
                    binding.tvStatus.text = "ನೀಡಲಾಗಿದೆ - $daysLeft ದಿನ ಬಾಕಿ"
                    binding.tvStatus.setTextColor(Color.parseColor("#1565C0"))
                    binding.tvDueDate.setTextColor(Color.BLACK)
                }
            }

            // Return button (visible only for active issues)
            if (onReturnClick != null && tx.status != TransactionStatus.RETURNED) {
                binding.btnReturn.visibility = android.view.View.VISIBLE
                binding.btnReturn.setOnClickListener { onReturnClick.invoke(tx) }
            } else {
                binding.btnReturn.visibility = android.view.View.GONE
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem == newItem
    }
}
