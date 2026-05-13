package com.example.nammapustaka1.ui.mybooks

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nammapustaka1.adapter.TransactionAdapter
import com.example.nammapustaka1.data.model.Student
import com.example.nammapustaka1.data.model.TransactionStatus
import com.example.nammapustaka1.databinding.FragmentMyBooksBinding
import com.example.nammapustaka1.ui.qrscan.QRScanActivity
import com.example.nammapustaka1.viewmodel.LibraryViewModel
import com.example.nammapustaka1.viewmodel.LibraryViewModelFactory

class MyBooksFragment : Fragment() {

    private var _binding: FragmentMyBooksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by activityViewModels {
        LibraryViewModelFactory(requireActivity().application)
    }

    private lateinit var transactionAdapter: TransactionAdapter
    private var bookTitleMap: MutableMap<Long, String> = mutableMapOf()
    private var studentNameMap: MutableMap<Long, String> = mutableMapOf()
    private var studentList: List<Student> = emptyList()
    private var selectedStudentId: Long? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyBooksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTransactionList()
        observeData()

        binding.btnSelectStudent.setOnClickListener { showStudentPicker() }

        binding.btnQrIssue.setOnClickListener {
            val intent = Intent(requireContext(), QRScanActivity::class.java)
            intent.putExtra("mode", QRScanActivity.MODE_FIND_BOOK)
            startActivity(intent)
        }

        binding.chipAll.setOnClickListener { observeAllTransactions() }
        binding.chipActive.setOnClickListener { observeActiveTransactions() }
        binding.chipOverdue.setOnClickListener { observeOverdueTransactions() }
    }

    private fun setupTransactionList() {
        transactionAdapter = TransactionAdapter(
            bookTitleMap = bookTitleMap,
            studentNameMap = studentNameMap,
            onReturnClick = { tx ->
                val pages = viewModel.getBookById(tx.bookId).value?.totalPages ?: 0
                AlertDialog.Builder(requireContext())
                    .setTitle("ಪುಸ್ತಕ ಹಿಂತಿರುಗಿಸು")
                    .setMessage("ಈ ಪುಸ್ತಕ ಹಿಂತಿರುಗಿಸಲಾಗಿದೆ ಎಂದು ದಾಖಲಿಸಬೇಕೇ?")
                    .setPositiveButton("ಹೌದು") { _, _ ->
                        viewModel.returnBook(tx.id, tx.studentId, pages)
                    }
                    .setNegativeButton("ಇಲ್ಲ", null)
                    .show()
            }
        )
        binding.rvTransactions.adapter = transactionAdapter
    }

    private fun observeData() {
        viewModel.allBooks.observe(viewLifecycleOwner) { books ->
            books.forEach { bookTitleMap[it.id] = it.title }
        }
        viewModel.allStudents.observe(viewLifecycleOwner) { students ->
            studentList = students
            students.forEach { studentNameMap[it.id] = it.name }
        }
        observeAllTransactions()
    }

    private fun observeAllTransactions() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { txList ->
            val updated = transactionAdapter.currentList.toMutableList()
            transactionAdapter.submitList(txList)
            binding.tvEmptyState.visibility = if (txList.isEmpty()) View.VISIBLE else View.GONE
            binding.tvCount.text = "ಒಟ್ಟು ${txList.size} ವ್ಯವಹಾರಗಳು"
        }
    }

    private fun observeActiveTransactions() {
        viewModel.getTransactionsByStatus(TransactionStatus.ISSUED).observe(viewLifecycleOwner) { txList ->
            transactionAdapter.submitList(txList)
            binding.tvEmptyState.visibility = if (txList.isEmpty()) View.VISIBLE else View.GONE
            binding.tvCount.text = "${txList.size} ಸಕ್ರಿಯ ಪುಸ್ತಕಗಳು"
        }
    }

    private fun observeOverdueTransactions() {
        viewModel.getTransactionsByStatus(TransactionStatus.OVERDUE).observe(viewLifecycleOwner) { txList ->
            transactionAdapter.submitList(txList)
            binding.tvEmptyState.visibility = if (txList.isEmpty()) View.VISIBLE else View.GONE
            binding.tvCount.text = "${txList.size} ತಡವಾದ ಪುಸ್ತಕಗಳು"
        }
    }

    private fun showStudentPicker() {
        if (studentList.isEmpty()) {
            Toast.makeText(requireContext(), "ಯಾವ ವಿದ್ಯಾರ್ಥಿಗಳೂ ಇಲ್ಲ", Toast.LENGTH_SHORT).show()
            return
        }
        val names = studentList.map { "${it.name} (${it.rollNumber})" }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("ವಿದ್ಯಾರ್ಥಿ ಆಯ್ಕೆ ಮಾಡಿ")
            .setItems(names) { _, which ->
                val student = studentList[which]
                selectedStudentId = student.id
                binding.btnSelectStudent.text = student.name
                viewModel.getActiveTransactionsByStudent(student.id).observe(viewLifecycleOwner) { txList ->
                    transactionAdapter.submitList(txList)
                    binding.tvCount.text = "${txList.size} ಪುಸ್ತಕಗಳು ನೀಡಲಾಗಿದೆ"
                }
            }
            .setNegativeButton("ರದ್ದು", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
