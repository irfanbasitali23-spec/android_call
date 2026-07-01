package com.esibil.call

import android.app.Application
import com.esibil.call.core.LinphoneManager
import com.esibil.call.data.Prefs
import com.esibil.call.service.SipForegroundService

class CallApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Warm up the Linphone Core early so registration is fast.
        LinphoneManager.getInstance(this)

        // If the user has previously provisioned credentials, spin up the
        // background SIP service so the app keeps receiving calls while in
        // the background / after the UI is killed.
        val prefs = Prefs(this)
        if (prefs.isRegistered) {
            SipForegroundService.start(this)
        }
    }
}