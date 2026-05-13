package com.example.nammapustaka1.ui.catalog

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.example.nammapustaka1.R
import com.example.nammapustaka1.adapter.BookGridAdapter
import com.example.nammapustaka1.databinding.FragmentCatalogBinding
import com.example.nammapustaka1.ui.addbbook.AddBookActivity
import com.example.nammapustaka1.ui.detail.BookDetailActivity
import com.example.nammapustaka1.viewmodel.LibraryViewModel
import com.example.nammapustaka1.viewmodel.LibraryViewModelFactory

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by activityViewModels {
        LibraryViewModelFactory(requireActivity().application)
    }

    private lateinit var bookAdapter: BookGridAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupCategoryChips()
        setupFab()
        observeBooks()

        // Search menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_catalog, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = "ಹೆಸರು ಅಥವಾ ಲೇಖಕ ಹುಡುಕಿ..."
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.searchBooks(newText ?: "")
                        return true
                    }
                })
            }
            override fun onMenuItemSelected(menuItem: MenuItem) = false
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        bookAdapter = BookGridAdapter(
            onBookClick = { book ->
                val intent = Intent(requireContext(), BookDetailActivity::class.java)
                intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.id)
                startActivity(intent)
            }
        )
        binding.rvBooks.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = bookAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearch() {
        // Handled in MenuProvider above
    }

    private fun setupCategoryChips() {
        // "All" chip
        addCategoryChip("ಎಲ್ಲಾ", null)

        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            // Remove old category chips (keep "All" at position 0)
            while (binding.chipGroupCategories.childCount > 1) {
                binding.chipGroupCategories.removeViewAt(1)
            }
            categories.forEach { category ->
                addCategoryChip(category, category)
            }
        }
    }

    private fun addCategoryChip(label: String, category: String?) {
        val chip = Chip(requireContext()).apply {
            text = label
            isCheckable = true
            isChecked = category == null
            setOnClickListener {
                viewModel.filterByCategory(category)
                // Uncheck all siblings
                for (i in 0 until binding.chipGroupCategories.childCount) {
                    val c = binding.chipGroupCategories.getChildAt(i) as? Chip
                    c?.isChecked = c == this
                }
            }
        }
        binding.chipGroupCategories.addView(chip)
    }

    private fun setupFab() {
        binding.fabAddBook.setOnClickListener {
            startActivity(Intent(requireContext(), AddBookActivity::class.java))
        }
    }

    private fun observeBooks() {
        viewModel.searchResults.observe(viewLifecycleOwner) { books ->
            bookAdapter.submitList(books)
            binding.tvEmptyState.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.totalBookCount.observe(viewLifecycleOwner) { count ->
            binding.tvTotalBooks.text = "ಒಟ್ಟು $count ಪುಸ್ತಕಗಳು"
        }

        viewModel.activeBorrowCount.observe(viewLifecycleOwner) { count ->
            binding.tvActiveBorrows.text = "$count ನೀಡಲಾಗಿದೆ"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
