package com.esibil.call.data

import android.content.Context

/** Persistent store for SIP credentials, profile info and onboarding state. */
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

    var displayName: String?
        get() = sp.getString(KEY_DISPLAY_NAME, null)
        set(v) = sp.edit().putString(KEY_DISPLAY_NAME, v).apply()

    var jailName: String?
        get() = sp.getString(KEY_JAIL_NAME, null)
        set(v) = sp.edit().putString(KEY_JAIL_NAME, v).apply()

    var jailIp: String?
        get() = sp.getString(KEY_JAIL_IP, null)
        set(v) = sp.edit().putString(KEY_JAIL_IP, v).apply()

    var isSetupComplete: Boolean
        get() = sp.getBoolean(KEY_SETUP_COMPLETE, false)
        set(v) = sp.edit().putBoolean(KEY_SETUP_COMPLETE, v).apply()

    val isRegistered: Boolean
        get() = !sipId.isNullOrBlank() && !password.isNullOrBlank()

    /** True when the user finished onboarding and has valid SIP creds saved. */
    val isLoggedIn: Boolean
        get() = isSetupComplete && isRegistered

    fun photoPath(index: Int): String? =
        sp.getString("$KEY_PHOTO_PREFIX$index", null)

    fun setPhotoPath(index: Int, path: String?) {
        sp.edit().putString("$KEY_PHOTO_PREFIX$index", path).apply()
    }

    fun save(phone: String, sipId: String, password: String, domain: String) {
        sp.edit()
            .putString(KEY_PHONE, phone)
            .putString(KEY_SIP_ID, sipId)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_DOMAIN, domain)
            .apply()
    }

    fun saveProfile(
        phone: String,
        sipId: String,
        password: String,
        domain: String,
        displayName: String,
        jailName: String,
        jailIp: String
    ) {
        sp.edit()
            .putString(KEY_PHONE, phone)
            .putString(KEY_SIP_ID, sipId)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_DOMAIN, domain)
            .putString(KEY_DISPLAY_NAME, displayName)
            .putString(KEY_JAIL_NAME, jailName)
            .putString(KEY_JAIL_IP, jailIp)
            .putBoolean(KEY_SETUP_COMPLETE, true)
            .apply()
    }

    fun clear() = sp.edit().clear().apply()

    companion object {
        private const val KEY_PHONE = "phone"
        private const val KEY_SIP_ID = "sip_id"
        private const val KEY_PASSWORD = "password"
        private const val KEY_DOMAIN = "domain"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_JAIL_NAME = "jail_name"
        private const val KEY_JAIL_IP = "jail_ip"
        private const val KEY_SETUP_COMPLETE = "setup_complete"
        private const val KEY_PHOTO_PREFIX = "photo_path_"
    }
}
