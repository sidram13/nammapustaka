package com.example.nammapustaka1.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nammapustaka1.R
import com.example.nammapustaka1.data.model.Student
import com.example.nammapustaka1.databinding.ItemLeaderboardBinding

class LeaderboardAdapter(
    private val onStudentClick: (Student) -> Unit = {}
) : ListAdapter<Student, LeaderboardAdapter.LeaderboardViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class LeaderboardViewHolder(private val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student, rank: Int) {
            binding.tvRank.text = "#$rank"
            binding.tvStudentName.text = student.name
            binding.tvClass.text = "${student.className} - ${student.section}"
            binding.tvPagesRead.text = "${student.totalPagesRead} ಪುಟಗಳು"
            binding.tvBooksRead.text = "${student.booksRead} ಪುಸ್ತಕಗಳು"

            // Highlight top 3
            val rankColor = when (rank) {
                1 -> ContextCompat.getColor(binding.root.context, R.color.gold)
                2 -> ContextCompat.getColor(binding.root.context, R.color.silver)
                3 -> ContextCompat.getColor(binding.root.context, R.color.bronze)
                else -> ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
            }
            binding.tvRank.setTextColor(rankColor)

            val rankEmoji = when (rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "#$rank"
            }
            binding.tvRank.text = rankEmoji

            binding.root.setOnClickListener { onStudentClick(student) }
        }
    }

    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Student, newItem: Student) = oldItem == newItem
    }
}
