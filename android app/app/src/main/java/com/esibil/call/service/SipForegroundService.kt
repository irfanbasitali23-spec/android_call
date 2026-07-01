package com.esibil.call.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.esibil.call.Config
import com.esibil.call.R
import com.esibil.call.core.LinphoneManager
import com.esibil.call.data.Prefs
import com.esibil.call.ui.CallActivity
import org.linphone.core.Call
import org.linphone.core.RegistrationState

/**
 * Background SIP service.
 *
 * Started from [com.esibil.call.CallApplication] whenever the process is
 * alive (and once at boot from a BOOT_COMPLETED receiver if needed). Its job:
 *
 *  1. Re-register the saved SIP account with [LinphoneManager] so the server
 *     keeps us in the registry while the app is in the background / closed.
 *  2. Stay alive as a foreground service so Android won't kill the
 *     [LinphoneManager] core while we're waiting for an incoming call.
 *  3. When an incoming call arrives, post a full-screen high-priority
 *     notification that wakes the device and routes the user to
 *     [CallActivity] so the call can be answered.
 *
 * Sticky (`START_STICKY`) so the system restarts us after a low-memory kill.
 */
class SipForegroundService : Service(), LinphoneManager.Listener {

    private lateinit var linphone: LinphoneManager
    private lateinit var prefs: Prefs

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        linphone = LinphoneManager.getInstance(this)
        prefs = Prefs(this)
        linphone.addListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannel()

        val notification = buildOngoingNotification()
        // On Android 14+ the foreground service type must be declared both in
        // the manifest and at runtime via ServiceInfo — otherwise startForeground
        // throws SecurityException. We use dataSync (not phoneCall) because this
        // is a SIP/VoIP client, not the system dialer.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIF_ID_RUNNING,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            @Suppress("DEPRECATION")
            startForeground(NOTIF_ID_RUNNING, notification)
        }

        // Re-register with the saved credentials so we're reachable for calls
        // even after a process kill or device reboot.
        if (prefs.isRegistered) {
            val sipId = prefs.sipId!!
            val password = prefs.password!!
            val domain = prefs.domain ?: Config.sipDomain
            linphone.register(sipId, password, domain)
            Log.i(TAG, "SIP service re-registered as $sipId@$domain")
        } else {
            Log.w(TAG, "SIP service started but no credentials saved yet")
        }

        // If the service is restarted by the system after a kill, onStartCommand
        // gets called with a null intent — re-register so we re-attach.
        return START_STICKY
    }

    override fun onDestroy() {
        linphone.removeListener(this)
        super.onDestroy()
    }

    // ---- LinphoneManager.Listener ----------------------------------------

    override fun onRegistrationChanged(state: RegistrationState, message: String) {
        Log.i(TAG, "registration -> $state ($message)")
    }

    override fun onCallStateChanged(call: Call, state: Call.State, message: String) {
        if (state == Call.State.IncomingReceived) {
            showIncomingCallNotification(call)
        } else if (state == Call.State.End ||
            state == Call.State.Released ||
            state == Call.State.Error
        ) {
            cancelIncomingCallNotification()
        }
    }

    // ---- Notifications ----------------------------------------------------

    private fun ensureChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val incoming = NotificationChannel(
            CHANNEL_INCOMING,
            getString(R.string.sip_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.sip_channel_desc)
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setBypassDnd(false)
        }

        val ongoing = NotificationChannel(
            CHANNEL_RUNNING,
            getString(R.string.sip_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.sip_channel_desc)
            setShowBadge(false)
        }

        nm.createNotificationChannels(listOf(incoming, ongoing))
    }

    private fun buildOngoingNotification(): Notification {
        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pi = openAppIntent?.let {
            PendingIntent.getActivity(
                this, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_RUNNING)
            .setSmallIcon(R.drawable.ic_camera_monitor)
            .setContentTitle(getString(R.string.sip_service_title))
            .setContentText(getString(R.string.sip_service_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setColor(getColor(R.color.notification_icon_color))
            .setContentIntent(pi)
            .build()
    }

    private fun showIncomingCallNotification(call: Call) {
        val caller = call.remoteAddress?.username
            ?: call.remoteAddress?.asStringUriOnly()
            ?: getString(R.string.incoming_call)

        val video = call.remoteParams?.isVideoEnabled == true
        val fullScreenIntent = Intent(this, CallActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(CallActivity.EXTRA_INCOMING, true)
            .putExtra(CallActivity.EXTRA_VIDEO, video)

        val fullScreenPi = PendingIntent.getActivity(
            this, REQ_INCOMING, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_INCOMING)
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle(getString(R.string.incoming_notification_title))
            .setContentText(caller)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPi, true)
            .setContentIntent(fullScreenPi)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(getColor(R.color.notification_icon_color))
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use the system incoming-call screen on Android 8+ when available.
            builder.setColorized(false)
        }

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID_INCOMING, builder.build())
    }

    private fun cancelIncomingCallNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIF_ID_INCOMING)
    }

    companion object {
        private const val TAG = "SipForegroundService"

        const val CHANNEL_INCOMING = "sip_incoming"
        const val CHANNEL_RUNNING = "sip_running"

        private const val NOTIF_ID_RUNNING = 1001
        private const val NOTIF_ID_INCOMING = 1002

        private const val REQ_INCOMING = 2001

        /** Convenience helper used by [com.esibil.call.CallApplication]. */
        fun start(context: Context) {
            val intent = Intent(context, SipForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}