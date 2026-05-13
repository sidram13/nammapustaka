package com.example.nammapustaka1.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nammapustaka1.R
import com.example.nammapustaka1.data.model.Book
import com.example.nammapustaka1.databinding.ItemBookGridBinding

class BookGridAdapter(
    private val onBookClick: (Book) -> Unit,
    private val onBookLongClick: (Book) -> Boolean = { false }
) : ListAdapter<Book, BookGridAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(private val binding: ItemBookGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.tvBookTitle.text = book.title
            binding.tvBookAuthor.text = book.author
            binding.tvCategory.text = book.category

            // Availability badge
            if (book.availableCopies > 0) {
                binding.tvAvailability.text = "ಲಭ್ಯ"
                binding.tvAvailability.setTextColor(Color.parseColor("#2E7D32"))
                binding.tvAvailability.setBackgroundResource(R.drawable.bg_available)
            } else {
                binding.tvAvailability.text = "ಲಭ್ಯವಿಲ್ಲ"
                binding.tvAvailability.setTextColor(Color.parseColor("#C62828"))
                binding.tvAvailability.setBackgroundResource(R.drawable.bg_unavailable)
            }

            // Load book cover
            if (book.coverUrl.isNotBlank()) {
                Glide.with(binding.root.context)
                    .load(book.coverUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(binding.ivBookCover)
            } else {
                binding.ivBookCover.setImageResource(R.drawable.ic_book_placeholder)
            }

            binding.root.setOnClickListener { onBookClick(book) }
            binding.root.setOnLongClickListener { onBookLongClick(book) }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
    }
}
