package com.esibil.call.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.esibil.call.data.Prefs

/**
 * Re-launches [SipForegroundService] after the device finishes booting so the
 * user stays reachable for SIP calls even if the app hasn't been opened since
 * the last reboot.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        // Only bother re-launching if the user previously provisioned a SIP
        // account — otherwise there's nothing to register with.
        if (Prefs(context).isRegistered) {
            SipForegroundService.start(context)
        }
    }
}