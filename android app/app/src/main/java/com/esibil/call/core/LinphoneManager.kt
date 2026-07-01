package com.esibil.call.core

import android.content.Context
import android.util.Log
import com.esibil.call.Config
import org.linphone.core.Account
import org.linphone.core.AccountParams
import org.linphone.core.Address
import org.linphone.core.AudioDevice
import org.linphone.core.AuthInfo
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.MediaEncryption
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType

/**
 * Wraps the Linphone [Core] — the SIP/RTP engine that talks to the eSIBiL /
 * SIP calling server. Handles account registration and audio + video calls.
 *
 * Use as a singleton via [LinphoneManager.getInstance].
 */
class LinphoneManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    val core: Core

    // ---- Observable state -------------------------------------------------
    interface Listener {
        fun onRegistrationChanged(state: RegistrationState, message: String) {}
        fun onCallStateChanged(call: Call, state: Call.State, message: String) {}
    }

    private val listeners = mutableListOf<Listener>()
    fun addListener(l: Listener) { if (!listeners.contains(l)) listeners.add(l) }
    fun removeListener(l: Listener) { listeners.remove(l) }

    val currentCall: Call? get() = core.currentCall ?: core.calls.firstOrNull()

    private val coreListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core, account: Account, state: RegistrationState, message: String
        ) {
            Log.i(TAG, "Registration: $state ($message)")
            listeners.toList().forEach { it.onRegistrationChanged(state, message) }
        }

        override fun onCallStateChanged(
            core: Core, call: Call, state: Call.State, message: String
        ) {
            Log.i(TAG, "Call: $state ($message)")
            listeners.toList().forEach { it.onCallStateChanged(call, state, message) }
        }
    }

    init {
        val factory = Factory.instance()
        factory.setDebugMode(true, "ESIBiLCall")
        core = factory.createCore(null, null, appContext)
        core.addListener(coreListener)

        // Enable video globally (per-call flags still decide audio vs video).
        core.isVideoCaptureEnabled = true
        core.isVideoDisplayEnabled = true

        val policy = factory.createVideoActivationPolicy()
        policy.automaticallyAccept = true      // accept incoming video offers
        policy.automaticallyInitiate = false   // but don't force video on outgoing
        core.videoActivationPolicy = policy

        core.start()
    }

    // ---- Registration -----------------------------------------------------

    /**
     * Registers [sipId] against the hardcoded [Config] SIP server using the
     * provisioned [password]. Any previous account is cleared first.
     */
    fun register(sipId: String, password: String, domain: String = Config.SIP_DOMAIN) {
        clearAccounts()

        val authInfo: AuthInfo = Factory.instance().createAuthInfo(
            sipId,        // username
            null,         // userid
            password,     // password
            null,         // ha1
            null,         // realm (let the server tell us)
            domain        // domain
        )
        core.addAuthInfo(authInfo)

        val params: AccountParams = core.createAccountParams()

        val identity: Address? = Factory.instance().createAddress("sip:$sipId@$domain")
        identity?.let { params.identityAddress = it }

        val proxy: Address? = Factory.instance().createAddress(Config.SIP_PROXY)
        proxy?.transport = when (Config.SIP_TRANSPORT) {
            Config.Transport.Udp -> TransportType.Udp
            Config.Transport.Tcp -> TransportType.Tcp
            Config.Transport.Tls -> TransportType.Tls
        }
        proxy?.let { params.serverAddress = it }

        params.isRegisterEnabled = true

        val account: Account = core.createAccount(params)
        core.addAccount(account)
        core.defaultAccount = account
    }

    private fun clearAccounts() {
        core.clearAccounts()
        core.clearAllAuthInfo()
    }

    fun unregister() {
        core.defaultAccount?.let { acc ->
            val p = acc.params.clone()
            p.isRegisterEnabled = false
            acc.params = p
        }
    }

    // ---- Placing calls ----------------------------------------------------

    /** Dials [remote] (a bare SIP id or full sip: URI) with or without video. */
    fun startCall(remote: String, video: Boolean): Call? {
        val address = resolveAddress(remote) ?: return null
        val params = core.createCallParams(null) ?: return null
        params.isVideoEnabled = video
        params.mediaEncryption = MediaEncryption.None
        return core.inviteAddressWithParams(address, params)
    }

    private fun resolveAddress(remote: String): Address? {
        val uri = if (remote.startsWith("sip:")) remote
        else "sip:$remote@${core.defaultAccount?.params?.domain ?: Config.SIP_DOMAIN}"
        return core.interpretUrl(uri) ?: Factory.instance().createAddress(uri)
    }

    // ---- In-call controls -------------------------------------------------

    fun answer(video: Boolean = false) {
        val call = currentCall ?: return
        val params = core.createCallParams(call)
        params?.isVideoEnabled = video
        call.acceptWithParams(params)
    }

    fun decline() { currentCall?.decline(org.linphone.core.Reason.Declined) }

    fun hangUp() {
        if (core.callsNb == 0) return
        (core.currentCall ?: core.calls.firstOrNull())?.terminate()
    }

    fun setMicMuted(muted: Boolean) { core.isMicEnabled = !muted }
    fun isMicMuted(): Boolean = !core.isMicEnabled

    /** Route audio to the loudspeaker (true) or earpiece (false). */
    fun setSpeaker(on: Boolean) {
        val type = if (on) AudioDevice.Type.Speaker else AudioDevice.Type.Earpiece
        core.audioDevices.firstOrNull {
            it.type == type && it.hasCapability(AudioDevice.Capabilities.CapabilityPlay)
        }?.let { core.outputAudioDevice = it }
    }

    /** Enable/disable sending our video mid-call (video escalation). */
    fun setVideoEnabled(enabled: Boolean) {
        val call = currentCall ?: return
        val params = core.createCallParams(call) ?: return
        params.isVideoEnabled = enabled
        call.update(params)
    }

    fun isVideoCall(): Boolean = currentCall?.currentParams?.isVideoEnabled == true

    fun switchCamera() {
        val current = core.videoDevice
        val next = core.videoDevicesList.firstOrNull { it != current && it != "StaticImage: Static picture" }
        next?.let { core.videoDevice = it }
    }

    // ---- Video surfaces ---------------------------------------------------

    fun setVideoWindow(remote: Any?) { core.nativeVideoWindowId = remote }
    fun setPreviewWindow(local: Any?) { core.nativePreviewWindowId = local }

    companion object {
        private const val TAG = "LinphoneManager"

        @Volatile private var instance: LinphoneManager? = null

        fun getInstance(context: Context): LinphoneManager =
            instance ?: synchronized(this) {
                instance ?: LinphoneManager(context).also { instance = it }
            }
    }
}
