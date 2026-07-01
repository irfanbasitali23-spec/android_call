package com.esibil.call.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.esibil.call.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) refreshList()
    }

    private fun refreshList() {
        val records = CallHistoryHelper.loadRecords(requireContext())
        binding.recyclerCallHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCallHistory.adapter = CallHistoryAdapter(records)

        val (total, completed, minutes) = CallHistoryHelper.updateStats(records)
        binding.tvTotalCalls.text = total.toString()
        binding.tvCompletedCalls.text = completed.toString()
        binding.tvTotalMinutes.text = minutes.toString()
        binding.tvEmptyHistory.isVisible = records.isEmpty()
        binding.recyclerCallHistory.isVisible = records.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
