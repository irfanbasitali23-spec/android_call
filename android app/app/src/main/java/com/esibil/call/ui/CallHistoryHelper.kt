package com.esibil.call.ui

import android.content.Context
import com.esibil.call.core.LinphoneManager
import com.esibil.call.data.UserProfile
import org.linphone.core.CallLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Loads [CallRecord] entries from the Linphone native call log. */
object CallHistoryHelper {

    fun loadRecords(context: Context): List<CallRecord> {
        val logs = LinphoneManager.getInstance(context).core.callLogs
            ?: return emptyList()

        val dateFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())

        return logs
            .sortedByDescending { toMillis(it.startDate) }
            .map { log ->
                val millis = toMillis(log.startDate)
                val date = Date(millis)
                val remote = log.remoteAddress?.username
                    ?: log.remoteAddress?.displayName
                    ?: log.remoteAddress?.asStringUriOnly()
                    ?: "Unknown"

                CallRecord(
                    name = remote,
                    prisonerId = UserProfile.INMATE_ID,
                    location = UserProfile.JAIL_NAME,
                    date = dateFmt.format(date),
                    time = timeFmt.format(date),
                    duration = formatDuration(log.duration),
                    status = mapStatus(log)
                )
            }
    }

    fun updateStats(
        records: List<CallRecord>
    ): Triple<Int, Int, Int> {
        var completed = 0
        var totalSeconds = 0
        for (record in records) {
            if (record.status == CallRecord.Status.COMPLETED) {
                completed++
                totalSeconds += parseDurationToSeconds(record.duration)
            }
        }
        return Triple(records.size, completed, totalSeconds / 60)
    }

    fun parseDurationToSeconds(duration: String?): Int {
        if (duration.isNullOrEmpty() || !duration.contains(":")) return 0
        val parts = duration.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun mapStatus(log: CallLog): CallRecord.Status {
        return when (log.status) {
            CallLog.Status.Missed -> CallRecord.Status.MISSED
            CallLog.Status.Declined,
            CallLog.Status.Aborted,
            CallLog.Status.EarlyAborted -> CallRecord.Status.REJECTED
            CallLog.Status.Success -> {
                if (log.duration > 0) CallRecord.Status.COMPLETED
                else CallRecord.Status.MISSED
            }
            else -> {
                if (log.duration > 0) CallRecord.Status.COMPLETED
                else CallRecord.Status.MISSED
            }
        }
    }

    private fun formatDuration(seconds: Int): String? {
        if (seconds <= 0) return null
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", m, s)
    }

    /** Linphone versions differ on seconds vs milliseconds — normalise here. */
    private fun toMillis(startDate: Long): Long =
        if (startDate < 10_000_000_000L) startDate * 1000L else startDate
}
