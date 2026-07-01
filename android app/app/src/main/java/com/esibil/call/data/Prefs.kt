package com.esibil.call.data

import android.content.Context

/** Simple persistent store for the provisioned SIP credentials. */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("esibil_call", Context.MODE_PRIVATE)

    var phone: String?
        get() = sp.getString(KEY_PHONE, null)
        set(v) = sp.edit().putString(KEY_PHONE, v).apply()

    var sipId: String?
        get() = sp.getString(KEY_SIP_ID, null)
        set(v) = sp.edit().putString(KEY_SIP_ID, v).apply()

    var password: String?
        get() = sp.getString(KEY_PASSWORD, null)
        set(v) = sp.edit().putString(KEY_PASSWORD, v).apply()

    var domain: String?
        get() = sp.getString(KEY_DOMAIN, null)
        set(v) = sp.edit().putString(KEY_DOMAIN, v).apply()

    val isRegistered: Boolean
        get() = !sipId.isNullOrBlank() && !password.isNullOrBlank()

    fun save(phone: String, sipId: String, password: String, domain: String) {
        sp.edit()
            .putString(KEY_PHONE, phone)
            .putString(KEY_SIP_ID, sipId)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_DOMAIN, domain)
            .apply()
    }

    fun clear() = sp.edit().clear().apply()

    companion object {
        private const val KEY_PHONE = "phone"
        private const val KEY_SIP_ID = "sip_id"
        private const val KEY_PASSWORD = "password"
        private const val KEY_DOMAIN = "domain"
    }
}
