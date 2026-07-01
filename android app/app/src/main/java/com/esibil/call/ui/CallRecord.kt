package com.esibil.call.ui

/**
 * Immutable representation of a single call history entry shown in
 * [CallHistoryActivity] / [CallHistoryAdapter]. `duration` is null when the
 * call was not completed (missed / rejected).
 */
data class CallRecord(
    val name: String,
    val prisonerId: String,
    val location: String,
    val date: String,
    val time: String,
    val duration: String?, // null/empty when not applicable
    val status: Status
) {
    enum class Status {
        COMPLETED,
        MISSED,
        REJECTED
    }
}