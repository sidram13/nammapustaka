package com.example.nammapustaka1.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayout
import com.example.nammapustaka1.adapter.LeaderboardAdapter
import com.example.nammapustaka1.databinding.FragmentLeaderboardBinding
import com.example.nammapustaka1.viewmodel.LibraryViewModel
import com.example.nammapustaka1.viewmodel.LibraryViewModelFactory

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by activityViewModels {
        LibraryViewModelFactory(requireActivity().application)
    }

    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leaderboardAdapter = LeaderboardAdapter()
        binding.rvLeaderboard.adapter = leaderboardAdapter

        // Tab switching between pages-based and books-based rankings
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> observePageLeaderboard()
                    1 -> observeBookLeaderboard()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        observePageLeaderboard()

        // Month info
        val calendar = java.util.Calendar.getInstance()
        val month = calendar.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale("kn", "IN"))
            ?: java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault()).format(java.util.Date())
        binding.tvMonthTitle.text = "$month ತಿಂಗಳ ಓದಿನ ಶ್ರೇಣಿ"
    }

    private fun observePageLeaderboard() {
        viewModel.topReadersByPages.observe(viewLifecycleOwner) { students ->
            leaderboardAdapter.submitList(students)
            binding.tvEmptyState.visibility = if (students.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun observeBookLeaderboard() {
        viewModel.topReadersByBooks.observe(viewLifecycleOwner) { students ->
            leaderboardAdapter.submitList(students)
            binding.tvEmptyState.visibility = if (students.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
