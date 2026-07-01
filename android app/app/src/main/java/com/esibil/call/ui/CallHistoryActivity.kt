package com.esibil.call.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esibil.call.R
import com.esibil.call.databinding.ActivityCallHistoryBinding

/**
 * Lists past video calls (Completed / Missed / Rejected) for the connected
 * inmate and shows aggregate stats at the top.
 *
 * TODO: replace the sample data below with real records fetched from your
 * backend / call log (e.g. from your Linphone call history or an API call).
 */
class CallHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallHistoryBinding
    private lateinit var recyclerCallHistory: RecyclerView
    private lateinit var tvTotalCalls: TextView
    private lateinit var tvCompletedCalls: TextView
    private lateinit var tvTotalMinutes: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindViews()
        setupList()
    }

    private fun bindViews() {
        binding.btnBack.setOnClickListener { finish() }

        recyclerCallHistory = binding.recyclerCallHistory
        tvTotalCalls = binding.tvTotalCalls
        tvCompletedCalls = binding.tvCompletedCalls
        tvTotalMinutes = binding.tvTotalMinutes
    }

    private fun setupList() {
        val records = CallHistoryHelper.loadRecords(this)

        recyclerCallHistory.layoutManager = LinearLayoutManager(this)
        recyclerCallHistory.adapter = CallHistoryAdapter(records)

        val (total, completed, minutes) = CallHistoryHelper.updateStats(records)
        tvTotalCalls.text = total.toString()
        tvCompletedCalls.text = completed.toString()
        tvTotalMinutes.text = minutes.toString()
    }
}