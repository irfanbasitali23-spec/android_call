package com.esibil.call.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esibil.call.core.LinphoneManager
import com.esibil.call.data.Prefs
import com.esibil.call.databinding.ActivityDialerBinding
import org.linphone.core.Call
import org.linphone.core.RegistrationState

/**
 * Step 2: dial a SIP id and start an audio or video call. Also brings up the
 * in-call screen automatically for incoming calls.
 */
class DialerActivity : AppCompatActivity(), LinphoneManager.Listener {

    private lateinit var binding: ActivityDialerBinding
    private lateinit var linphone: LinphoneManager
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)
        linphone = LinphoneManager.getInstance(this)

        binding.myIdText.text = getString(
            com.esibil.call.R.string.registered_as, prefs.sipId ?: "-"
        )

        binding.audioCallButton.setOnClickListener { dial(video = false) }
        binding.videoCallButton.setOnClickListener { dial(video = true) }
        binding.logoutButton.setOnClickListener { logout() }
    }

    override fun onResume() {
        super.onResume()
        linphone.addListener(this)
    }

    override fun onPause() {
        linphone.removeListener(this)
        super.onPause()
    }

    private fun dial(video: Boolean) {
        val remote = binding.remoteInput.text?.toString()?.trim().orEmpty()
        if (remote.isEmpty()) {
            binding.remoteInput.error = getString(com.esibil.call.R.string.enter_sip_id)
            return
        }
        val call = linphone.startCall(remote, video)
        if (call == null) {
            Toast.makeText(this, com.esibil.call.R.string.call_failed, Toast.LENGTH_SHORT).show()
            return
        }
        openCallScreen(video)
    }

    private fun openCallScreen(video: Boolean) {
        startActivity(
            Intent(this, CallActivity::class.java)
                .putExtra(CallActivity.EXTRA_VIDEO, video)
        )
    }

    private fun logout() {
        linphone.unregister()
        prefs.clear()
        // Stop the background SIP service so we stop receiving calls
        // until the user registers again.
        stopService(Intent(this, com.esibil.call.service.SipForegroundService::class.java))
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }

    override fun onRegistrationChanged(state: RegistrationState, message: String) {}

    override fun onCallStateChanged(call: Call, state: Call.State, message: String) {
        if (state == Call.State.IncomingReceived) {
            runOnUiThread {
                startActivity(
                    Intent(this, CallActivity::class.java)
                        .putExtra(CallActivity.EXTRA_INCOMING, true)
                        .putExtra(CallActivity.EXTRA_VIDEO, call.remoteParams?.isVideoEnabled == true)
                )
            }
        }
    }
}
