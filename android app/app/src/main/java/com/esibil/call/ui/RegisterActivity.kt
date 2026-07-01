package com.esibil.call.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esibil.call.Config
import com.esibil.call.R
import com.esibil.call.core.LinphoneManager
import com.esibil.call.data.Prefs
import com.esibil.call.databinding.ActivityRegisterBinding
import org.linphone.core.Call
import org.linphone.core.RegistrationState

class RegisterActivity : AppCompatActivity(), LinphoneManager.Listener {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var prefs: Prefs
    private lateinit var linphone: LinphoneManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)
        linphone = LinphoneManager.getInstance(this)

        requestRuntimePermissions()

        binding.serverLabel.text = "Server : ${Config.SIP_DOMAIN}"

        // Hide phone input if you don't need it
        binding.phoneInput.visibility = View.GONE

        // Change button text
        binding.registerButton.text = "Register SIP"

        binding.registerButton.setOnClickListener {

            // -----------------------------
            // CHANGE THESE VALUES
            // -----------------------------
            val sipId = "1003"
            val password = "Winter2023$$"
            val domain = "110.39.151.35:5066"
            // -----------------------------

            prefs.save(
                phone = "",
                sipId = sipId,
                password = password,
                domain = domain
            )

            registerToSip()
        }
    }

    override fun onResume() {
        super.onResume()
        linphone.addListener(this)
    }

    override fun onPause() {
        linphone.removeListener(this)
        super.onPause()
    }

    private fun registerToSip() {

        val sipId = prefs.sipId ?: return
        val password = prefs.password ?: return
        val domain = prefs.domain ?: Config.SIP_DOMAIN

        setBusy(true, "Registering...")

        linphone.register(
            sipId,
            password,
            domain
        )
    }

    override fun onRegistrationChanged(
        state: RegistrationState,
        message: String
    ) {

        runOnUiThread {

            when (state) {

                RegistrationState.Progress -> {
                    setBusy(true, "Registering...")
                }

                RegistrationState.Ok -> {

                    setBusy(false, "Registered as ${prefs.sipId}")

                    startActivity(
                        Intent(
                            this@RegisterActivity,
                            DialerActivity::class.java
                        )
                    )

                    finish()
                }

                RegistrationState.Failed -> {
                    setBusy(
                        false,
                        "Registration Failed\n$message"
                    )
                }

                RegistrationState.Cleared -> {
                    setBusy(false, "Registration Cleared")
                }

                RegistrationState.None -> {
                    setBusy(false, "Not Registered")
                }

                else -> {
                    setBusy(false, message)
                }
            }
        }
    }

    override fun onCallStateChanged(
        call: Call,
        state: Call.State,
        message: String
    ) {
        // Nothing
    }

    private fun setBusy(
        busy: Boolean,
        status: String
    ) {

        binding.progress.visibility =
            if (busy) View.VISIBLE else View.GONE

        binding.registerButton.isEnabled = !busy

        binding.statusText.text = status
    }

    private fun requestRuntimePermissions() {

        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val toRequest = permissions.filter {

            ContextCompat.checkSelfPermission(
                this,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {

            ActivityCompat.requestPermissions(
                this,
                toRequest.toTypedArray(),
                100
            )
        }
    }
}