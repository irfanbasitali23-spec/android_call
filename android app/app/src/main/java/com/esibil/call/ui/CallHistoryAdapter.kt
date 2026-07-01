package com.esibil.call.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.esibil.call.R

/**
 * RecyclerView adapter for [CallRecord] items. Swaps the icon background, badge,
 * duration and banner per call status (Completed / Missed / Rejected).
 */
class CallHistoryAdapter(
    private val callRecords: List<CallRecord>
) : RecyclerView.Adapter<CallHistoryAdapter.CallViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_call_history, parent, false)
        return CallViewHolder(view)
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        holder.bind(callRecords[position])
    }

    override fun getItemCount(): Int = callRecords.size

    class CallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val circleIconBg: FrameLayout = itemView.findViewById(R.id.circleIconBg)
        private val ivVideoIcon: ImageView = itemView.findViewById(R.id.ivVideoIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPrisonerId: TextView = itemView.findViewById(R.id.tvPrisonerId)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val badgeContainer: LinearLayout = itemView.findViewById(R.id.badgeContainer)
        private val ivBadgeIcon: ImageView = itemView.findViewById(R.id.ivBadgeIcon)
        private val tvBadgeText: TextView = itemView.findViewById(R.id.tvBadgeText)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val layoutDuration: LinearLayout = itemView.findViewById(R.id.layoutDuration)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val layoutBanner: LinearLayout = itemView.findViewById(R.id.layoutBanner)
        private val ivBannerIcon: ImageView = itemView.findViewById(R.id.ivBannerIcon)
        private val tvBannerText: TextView = itemView.findViewById(R.id.tvBannerText)

        fun bind(record: CallRecord) {
            tvName.text = record.name
            tvPrisonerId.text = "Prisoner ID: ${record.prisonerId}"
            tvLocation.text = record.location
            tvDate.text = record.date
            tvTime.text = record.time

            val res = itemView.resources
            when (record.status) {
                CallRecord.Status.COMPLETED -> {
                    circleIconBg.setBackgroundResource(R.drawable.bg_circle_light_green)
                    ivVideoIcon.setImageResource(R.drawable.ic_video_camera)

                    badgeContainer.setBackgroundResource(R.drawable.bg_badge_completed)
                    ivBadgeIcon.setImageResource(R.drawable.ic_check_small_green)
                    tvBadgeText.text = "Completed"
                    tvBadgeText.setTextColor(res.getColor(R.color.badge_completed_text))

                    layoutDuration.visibility = View.VISIBLE
                    tvDuration.text = "Duration: ${record.duration}"
                    layoutBanner.visibility = View.GONE
                }

                CallRecord.Status.MISSED -> {
                    circleIconBg.setBackgroundResource(R.drawable.bg_circle_light_orange)
                    ivVideoIcon.setImageResource(R.drawable.ic_video_camera_orange)

                    badgeContainer.setBackgroundResource(R.drawable.bg_badge_missed)
                    ivBadgeIcon.setImageResource(R.drawable.ic_alert_small_orange)
                    tvBadgeText.text = "Missed"
                    tvBadgeText.setTextColor(res.getColor(R.color.badge_missed_text))

                    layoutDuration.visibility = View.GONE
                    layoutBanner.visibility = View.VISIBLE
                    layoutBanner.setBackgroundResource(R.drawable.bg_banner_missed)
                    ivBannerIcon.setImageResource(R.drawable.ic_info_orange)
                    tvBannerText.text = "You missed this scheduled call"
                    tvBannerText.setTextColor(res.getColor(R.color.banner_missed_text))
                }

                CallRecord.Status.REJECTED -> {
                    circleIconBg.setBackgroundResource(R.drawable.bg_circle_light_red)
                    ivVideoIcon.setImageResource(R.drawable.ic_video_camera_red)

                    badgeContainer.setBackgroundResource(R.drawable.bg_badge_rejected)
                    ivBadgeIcon.setImageResource(R.drawable.ic_alert_small_red)
                    tvBadgeText.text = "Rejected"
                    tvBadgeText.setTextColor(res.getColor(R.color.badge_rejected_text))

                    layoutDuration.visibility = View.GONE
                    layoutBanner.visibility = View.VISIBLE
                    layoutBanner.setBackgroundResource(R.drawable.bg_banner_rejected)
                    ivBannerIcon.setImageResource(R.drawable.ic_info_red)
                    tvBannerText.text = "Call was rejected by prison authorities"
                    tvBannerText.setTextColor(res.getColor(R.color.banner_rejected_text))
                }
            }
        }
    }
}