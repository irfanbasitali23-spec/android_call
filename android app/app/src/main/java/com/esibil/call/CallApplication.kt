package com.esibil.call

import android.app.Application
import com.esibil.call.core.LinphoneManager
import com.esibil.call.data.Prefs
import com.esibil.call.service.SipForegroundService

class CallApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LinphoneManager.getInstance(this)

        val prefs = Prefs(this)
        Config.restoreFromSavedDomain(prefs.domain)
        if (prefs.isRegistered) {
            SipForegroundService.start(this)
        }
    }
}
