package com.esibil.call.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esibil.call.core.LinphoneManager
import com.esibil.call.databinding.ActivityHomeBinding
import org.linphone.core.Call
import org.linphone.core.RegistrationState

/**
 * Dashboard shown after a successful login. Shows the connected inmate summary,
 * quick action cards (Call History / My Profile) and the bottom navigation.
 * Also listens for incoming SIP calls and automatically opens the in-call screen.
 */
class HomeActivity : AppCompatActivity(), LinphoneManager.Listener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var linphone: LinphoneManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        linphone = LinphoneManager.getInstance(this)

        bindData()
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
        linphone.addListener(this)
    }

    override fun onPause() {
        linphone.removeListener(this)
        super.onPause()
    }

    /**
     * TODO: replace with the real logged-in user's data.
     * Easiest path: pass it in as Intent extras from LoginActivity,
     * or pull it from your Prefs class (you already store SIP creds there).
     */
    private fun bindData() {
        val userName = "User"
        val prisonerNumber = "1234567"
        val location = "Central Jail Lahore"

        binding.tvUserName.text = userName
        binding.tvPrisonerHeadline.text = "Prisoner #$prisonerNumber"
        binding.tvPrisonerNumber.text = prisonerNumber
        binding.tvLocation.text = location
    }

    private fun setClickListeners() {
        binding.cardCallHistory.setOnClickListener {
            startActivity(Intent(this, CallHistoryActivity::class.java))
        }

        binding.cardMyProfile.setOnClickListener {
            // TODO: startActivity(Intent(this, ProfileActivity::class.java))
            Toast.makeText(this, "Open My Profile", Toast.LENGTH_SHORT).show()
        }

        binding.navHome.setOnClickListener {
            // Already on Home
        }

        binding.navHistory.setOnClickListener {
            startActivity(Intent(this, CallHistoryActivity::class.java))
        }

        binding.navProfile.setOnClickListener {
            Toast.makeText(this, "Open Profile", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRegistrationChanged(state: RegistrationState, message: String) {}

    override fun onCallStateChanged(call: Call, state: Call.State, message: String) {
        if (state == Call.State.IncomingReceived) {
            runOnUiThread { openIncomingCallScreen(call) }
        }
    }

    private fun openIncomingCallScreen(call: Call) {
        startActivity(
            Intent(this, CallActivity::class.java)
                .putExtra(CallActivity.EXTRA_INCOMING, true)
                .putExtra(CallActivity.EXTRA_VIDEO, call.remoteParams?.isVideoEnabled == true)
        )
    }
}