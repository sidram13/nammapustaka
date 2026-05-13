package com.example.nammapustaka1.ui.students

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nammapustaka1.data.model.Student
import com.example.nammapustaka1.databinding.FragmentStudentsBinding
import com.example.nammapustaka1.databinding.DialogAddStudentBinding
import com.example.nammapustaka1.viewmodel.LibraryViewModel
import com.example.nammapustaka1.viewmodel.LibraryViewModelFactory
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nammapustaka1.databinding.ItemStudentBinding

class StudentsFragment : Fragment() {

    private var _binding: FragmentStudentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by activityViewModels {
        LibraryViewModelFactory(requireActivity().application)
    }

    private lateinit var studentAdapter: StudentListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        studentAdapter = StudentListAdapter()
        binding.rvStudents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = studentAdapter
        }

        viewModel.allStudents.observe(viewLifecycleOwner) { students ->
            studentAdapter.submitList(students)
            binding.tvCount.text = "${students.size} ವಿದ್ಯಾರ್ಥಿಗಳು"
            binding.tvEmptyState.visibility = if (students.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddStudent.setOnClickListener {
            showAddStudentDialog()
        }
    }

    private fun showAddStudentDialog() {
        val dialogBinding = DialogAddStudentBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext())
            .setTitle("ಹೊಸ ವಿದ್ಯಾರ್ಥಿ ಸೇರಿಸಿ")
            .setView(dialogBinding.root)
            .setPositiveButton("ಸೇರಿಸಿ") { _, _ ->
                val name = dialogBinding.etName.text.toString().trim()
                val roll = dialogBinding.etRollNumber.text.toString().trim()
                val className = dialogBinding.etClass.text.toString().trim()
                val section = dialogBinding.etSection.text.toString().trim().ifBlank { "A" }

                if (name.isBlank()) { dialogBinding.etName.error = "ಹೆಸರು ಅಗತ್ಯ"; return@setPositiveButton }
                if (roll.isBlank()) { dialogBinding.etRollNumber.error = "ರೋಲ್ ಸಂಖ್ಯೆ ಅಗತ್ಯ"; return@setPositiveButton }

                viewModel.addStudent(Student(name = name, rollNumber = roll, className = className, section = section))
                Toast.makeText(requireContext(), "$name ಸೇರಿಸಲಾಗಿದೆ", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("ರದ್ದು", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class StudentListAdapter : ListAdapter<Student, StudentListAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class ViewHolder(private val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: Student) {
            binding.tvName.text = student.name
            binding.tvRoll.text = "ರೋಲ್: ${student.rollNumber}"
            binding.tvClass.text = "${student.className} - ${student.section}"
            binding.tvStats.text = "${student.booksRead} ಪುಸ್ತಕ • ${student.totalPagesRead} ಪುಟ"
        }
    }

    class Diff : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(o: Student, n: Student) = o.id == n.id
        override fun areContentsTheSame(o: Student, n: Student) = o == n
    }
}
