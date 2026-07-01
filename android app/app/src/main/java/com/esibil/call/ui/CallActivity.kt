package com.esibil.call.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.esibil.call.R
import com.esibil.call.core.LinphoneManager
import com.esibil.call.databinding.ActivityCallBinding
import org.linphone.core.Call
import org.linphone.core.RegistrationState

/**
 * The in-call screen. Handles both outgoing and incoming calls, audio or video,
 * with mute / speaker / camera / video-toggle / hang-up controls.
 */
class CallActivity : AppCompatActivity(), LinphoneManager.Listener {

    private lateinit var binding: ActivityCallBinding
    private lateinit var linphone: LinphoneManager

    private var videoEnabled = false
    private var speakerOn = false
    private var micMuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        linphone = LinphoneManager.getInstance(this)
        videoEnabled = intent.getBooleanExtra(EXTRA_VIDEO, false)
        val incoming = intent.getBooleanExtra(EXTRA_INCOMING, false)

        // Wire the video surfaces to the Linphone Core.
        linphone.setVideoWindow(binding.remoteVideo)
        linphone.setPreviewWindow(binding.localPreview)

        setupControls()
        renderRemoteName()
        applyVideoVisibility()

        if (incoming) showIncomingUi() else showOutgoingUi()

        speakerOn = videoEnabled                 // video calls default to loudspeaker
        linphone.setSpeaker(speakerOn)
        updateSpeakerIcon()
    }

    override fun onResume() {
        super.onResume()
        linphone.addListener(this)
        // Re-bind surfaces after the window is ready.
        linphone.setVideoWindow(binding.remoteVideo)
        linphone.setPreviewWindow(binding.localPreview)
    }

    override fun onPause() {
        linphone.removeListener(this)
        super.onPause()
    }

    // ---- Controls ---------------------------------------------------------

    private fun setupControls() {
        binding.hangupButton.setOnClickListener {
            linphone.hangUp()
            finish()
        }
        binding.answerButton.setOnClickListener {
            linphone.answer(videoEnabled)
            showOutgoingUi()
        }
        binding.declineButton.setOnClickListener {
            linphone.decline()
            finish()
        }
        binding.muteButton.setOnClickListener {
            micMuted = !micMuted
            linphone.setMicMuted(micMuted)
            binding.muteButton.setImageResource(
                if (micMuted) R.drawable.ic_mic_off else R.drawable.ic_mic
            )
        }
        binding.speakerButton.setOnClickListener {
            speakerOn = !speakerOn
            linphone.setSpeaker(speakerOn)
            updateSpeakerIcon()
        }
        binding.videoToggleButton.setOnClickListener {
            videoEnabled = !videoEnabled
            linphone.setVideoEnabled(videoEnabled)
            applyVideoVisibility()
        }
        binding.switchCameraButton.setOnClickListener { linphone.switchCamera() }
    }

    private fun updateSpeakerIcon() {
        binding.speakerButton.setImageResource(
            if (speakerOn) R.drawable.ic_speaker_on else R.drawable.ic_speaker_off
        )
    }

    private fun applyVideoVisibility() {
        val v = if (videoEnabled) View.VISIBLE else View.GONE
        binding.remoteVideo.visibility = v
        binding.localPreview.visibility = v
        binding.switchCameraButton.visibility = v
        binding.videoToggleButton.setImageResource(
            if (videoEnabled) R.drawable.ic_videocam else R.drawable.ic_videocam_off
        )
    }

    private fun renderRemoteName() {
        val remote = linphone.currentCall?.remoteAddress?.username
            ?: linphone.currentCall?.remoteAddress?.asStringUriOnly()
            ?: "-"
        binding.remoteName.text = remote
    }

    private fun showIncomingUi() {
        binding.incomingButtons.visibility = View.VISIBLE
        binding.inCallButtons.visibility = View.GONE
        binding.callStatus.setText(R.string.incoming_call)
    }

    private fun showOutgoingUi() {
        binding.incomingButtons.visibility = View.GONE
        binding.inCallButtons.visibility = View.VISIBLE
    }

    // ---- Call state -------------------------------------------------------

    override fun onCallStateChanged(call: Call, state: Call.State, message: String) {
        runOnUiThread {
            when (state) {
                Call.State.OutgoingProgress, Call.State.OutgoingInit ->
                    binding.callStatus.setText(R.string.calling)
                Call.State.OutgoingRinging -> binding.callStatus.setText(R.string.ringing)
                Call.State.Connected, Call.State.StreamsRunning -> {
                    binding.callStatus.setText(R.string.connected)
                    showOutgoingUi()
                    videoEnabled = linphone.isVideoCall()
                    applyVideoVisibility()
                }
                Call.State.End, Call.State.Released, Call.State.Error -> {
                    binding.callStatus.setText(R.string.call_ended)
                    finish()
                }
                else -> {}
            }
        }
    }

    override fun onRegistrationChanged(state: RegistrationState, message: String) {}

    companion object {
        const val EXTRA_VIDEO = "extra_video"
        const val EXTRA_INCOMING = "extra_incoming"
    }
}
