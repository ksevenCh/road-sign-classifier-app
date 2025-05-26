package com.example.roadsignclassifierapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roadsignclassifierapp.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = HistoryAdapter { entryToDelete ->
            showDeleteDialog(entryToDelete)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        loadHistory()

        binding.closeButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            val history = AppDatabase.getInstance(requireContext())
                .recognitionDao()
                .getAll()
            adapter.submitList(history)
        }
    }

    private fun showDeleteDialog(entry: RecognitionEntry) {
        val customView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(customView)
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    AppDatabase.getInstance(requireContext())
                        .recognitionDao()
                        .delete(entry)
                    loadHistory()
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color))
            typeface = ResourcesCompat.getFont(requireContext(), R.font.nunito_bold)
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
            setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color))
            typeface = ResourcesCompat.getFont(requireContext(), R.font.nunito_semi_bold)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}