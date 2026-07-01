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
        val records = listOf(
            CallRecord(
                "John Doe", "INM-2024-001", "Central Jail Lahore",
                "Mar 18, 2024", "2:30 PM", "28:45", CallRecord.Status.COMPLETED
            ),
            CallRecord(
                "John Doe", "INM-2024-001", "Central Jail Lahore",
                "Mar 15, 2024", "10:00 AM", "30:00", CallRecord.Status.COMPLETED
            ),
            CallRecord(
                "John Doe", "INM-2024-001", "Central Jail Lahore",
                "Mar 12, 2024", "3:00 PM", null, CallRecord.Status.MISSED
            ),
            CallRecord(
                "John Doe", "INM-2024-001", "Central Jail Lahore",
                "Mar 10, 2024", "11:30 AM", "25:12", CallRecord.Status.COMPLETED
            ),
            CallRecord(
                "John Doe", "INM-2024-001", "Central Jail Lahore",
                "Mar 8, 2024", "4:00 PM", null, CallRecord.Status.REJECTED
            )
        )

        recyclerCallHistory.layoutManager = LinearLayoutManager(this)
        recyclerCallHistory.adapter = CallHistoryAdapter(records)

        updateStats(records)
    }

    private fun updateStats(records: List<CallRecord>) {
        val total = records.size
        var completed = 0
        var totalSeconds = 0

        for (record in records) {
            if (record.status == CallRecord.Status.COMPLETED) {
                completed++
                totalSeconds += parseDurationToSeconds(record.duration)
            }
        }

        tvTotalCalls.text = total.toString()
        tvCompletedCalls.text = completed.toString()
        tvTotalMinutes.text = (totalSeconds / 60).toString()
    }

    private fun parseDurationToSeconds(duration: String?): Int {
        if (duration.isNullOrEmpty() || !duration.contains(":")) return 0
        val parts = duration.split(":")
        val minutes = parts[0].toInt()
        val seconds = parts[1].toInt()
        return minutes * 60 + seconds
    }
}