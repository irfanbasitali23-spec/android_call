package com.esibil.call

import android.content.Context
import android.util.Log
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.linphone.core.Account
import org.linphone.core.AccountParams
import org.linphone.core.Address
import org.linphone.core.AudioDevice
import org.linphone.core.AuthInfo
import org.linphone.core.Call
import org.linphone.core.CallLog
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.MediaEncryption
import org.linphone.core.Reason
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Linphone SIP bridge exposed to Flutter via MethodChannel / EventChannel.
 */
class LinphoneBridge(private val appContext: Context) :
    MethodChannel.MethodCallHandler,
    EventChannel.StreamHandler {

    private val core: Core
    private var eventSink: EventChannel.EventSink? = null
    private var sipDomain: String = DEFAULT_IP
    private var sipProxy: String = "sip:$DEFAULT_IP:$SIP_PORT"

    private val coreListener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState,
            message: String
        ) {
            Log.i(TAG, "Registration: $state ($message)")
            emit(mapOf(
                "type" to "registration",
                "state" to state.name,
                "message" to message
            ))
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State,
            message: String
        ) {
            Log.i(TAG, "Call: $state ($message)")
            val remote = call.remoteAddress?.username
                ?: call.remoteAddress?.asStringUriOnly()
            val video = call.remoteParams?.isVideoEnabled == true
                || call.currentParams?.isVideoEnabled == true
            emit(mapOf(
                "type" to "call",
                "state" to state.name,
                "message" to message,
                "remote" to remote,
                "video" to video
            ))
        }
    }

    init {
        val factory = Factory.instance()
        factory.setDebugMode(true, "ESIBiLCallFlutter")
        core = factory.createCore(null, null, appContext)
        core.addListener(coreListener)
        core.isVideoCaptureEnabled = true
        core.isVideoDisplayEnabled = true
        val policy = factory.createVideoActivationPolicy()
        policy.automaticallyAccept = true
        policy.automaticallyInitiate = false
        core.videoActivationPolicy = policy
        core.start()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        try {
            when (call.method) {
                "register" -> {
                    val sipId = call.argument<String>("sipId")
                        ?: return result.error("ARG", "sipId required", null)
                    val password = call.argument<String>("password")
                        ?: return result.error("ARG", "password required", null)
                    val domain = call.argument<String>("domain") ?: sipDomain
                    val proxy = call.argument<String>("proxy") ?: sipProxy
                    sipDomain = domain
                    sipProxy = proxy
                    register(sipId, password, domain, proxy)
                    result.success(null)
                }
                "unregister" -> {
                    unregister()
                    result.success(null)
                }
                "answer" -> {
                    val video = call.argument<Boolean>("video") ?: false
                    answer(video)
                    result.success(null)
                }
                "decline" -> {
                    decline()
                    result.success(null)
                }
                "hangUp" -> {
                    hangUp()
                    result.success(null)
                }
                "setSpeaker" -> {
                    val on = call.argument<Boolean>("on") ?: false
                    setSpeaker(on)
                    result.success(null)
                }
                "setMicMuted" -> {
                    val muted = call.argument<Boolean>("muted") ?: false
                    setMicMuted(muted)
                    result.success(null)
                }
                "startCall" -> {
                    val remote = call.argument<String>("remote")
                        ?: return result.error("ARG", "remote required", null)
                    val video = call.argument<Boolean>("video") ?: false
                    val ok = startCall(remote, video)
                    if (ok) result.success(null)
                    else result.error("CALL", "Could not start call", null)
                }
                "getCallLogs" -> {
                    result.success(getCallLogs())
                }
                else -> result.notImplemented()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Method ${call.method} failed", e)
            result.error("SIP", e.message, null)
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    fun dispose() {
        eventSink = null
        core.removeListener(coreListener)
        core.stop()
    }

    private fun emit(payload: Map<String, Any?>) {
        eventSink?.success(payload)
    }

    private fun register(sipId: String, password: String, domain: String, proxy: String) {
        clearAccounts()

        val authInfo: AuthInfo = Factory.instance().createAuthInfo(
            sipId, null, password, null, null, domain
        )
        core.addAuthInfo(authInfo)

        val params: AccountParams = core.createAccountParams()
        val identity: Address? = Factory.instance().createAddress("sip:$sipId@$domain")
        identity?.let { params.identityAddress = it }

        val proxyAddr: Address? = Factory.instance().createAddress(proxy)
        proxyAddr?.transport = TransportType.Udp
        proxyAddr?.let { params.serverAddress = it }

        params.isRegisterEnabled = true

        val account: Account = core.createAccount(params)
        core.addAccount(account)
        core.defaultAccount = account
    }

    private fun clearAccounts() {
        core.clearAccounts()
        core.clearAllAuthInfo()
    }

    private fun unregister() {
        core.defaultAccount?.let { acc ->
            val p = acc.params.clone()
            p.isRegisterEnabled = false
            acc.params = p
        }
    }

    private fun currentCall(): Call? = core.currentCall ?: core.calls.firstOrNull()

    private fun startCall(remote: String, video: Boolean): Boolean {
        val address = resolveAddress(remote) ?: return false
        val params = core.createCallParams(null) ?: return false
        params.isVideoEnabled = video
        params.mediaEncryption = MediaEncryption.None
        core.inviteAddressWithParams(address, params)
        return true
    }

    private fun resolveAddress(remote: String): Address? {
        val uri = if (remote.startsWith("sip:")) remote
        else "sip:$remote@${core.defaultAccount?.params?.domain ?: sipDomain}"
        return core.interpretUrl(uri) ?: Factory.instance().createAddress(uri)
    }

    private fun answer(video: Boolean) {
        val call = currentCall() ?: return
        val params = core.createCallParams(call)
        params?.isVideoEnabled = video
        call.acceptWithParams(params)
    }

    private fun decline() {
        currentCall()?.decline(Reason.Declined)
    }

    private fun hangUp() {
        if (core.callsNb == 0) return
        (core.currentCall ?: core.calls.firstOrNull())?.terminate()
    }

    private fun setMicMuted(muted: Boolean) {
        core.isMicEnabled = !muted
    }

    private fun setSpeaker(on: Boolean) {
        val type = if (on) AudioDevice.Type.Speaker else AudioDevice.Type.Earpiece
        core.audioDevices.firstOrNull {
            it.type == type && it.hasCapability(AudioDevice.Capabilities.CapabilityPlay)
        }?.let { core.outputAudioDevice = it }
    }

    private fun getCallLogs(): List<Map<String, Any?>> {
        val logs = core.callLogs ?: return emptyList()
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

                mapOf(
                    "name" to remote,
                    "prisonerId" to "-",
                    "location" to "-",
                    "date" to dateFmt.format(date),
                    "time" to timeFmt.format(time),
                    "duration" to formatDuration(log.duration),
                    "status" to mapStatus(log)
                )
            }
    }

    private fun mapStatus(log: CallLog): String {
        return when (log.status) {
            CallLog.Status.Missed -> "missed"
            CallLog.Status.Declined,
            CallLog.Status.Aborted,
            CallLog.Status.EarlyAborted -> "rejected"
            CallLog.Status.Success -> {
                if (log.duration > 0) "completed" else "missed"
            }
            else -> if (log.duration > 0) "completed" else "missed"
        }
    }

    private fun formatDuration(seconds: Int): String? {
        if (seconds <= 0) return null
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", m, s)
    }

    private fun toMillis(startDate: Long): Long =
        if (startDate < 10_000_000_000L) startDate * 1000L else startDate

    companion object {
        private const val TAG = "LinphoneBridge"
        private const val DEFAULT_IP = "110.39.151.34"
        private const val SIP_PORT = 5066
    }
}
