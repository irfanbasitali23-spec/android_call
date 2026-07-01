package com.esibil.call.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esibil.call.Config
import com.esibil.call.R
import com.esibil.call.core.LinphoneManager
import com.esibil.call.data.Prefs
import com.esibil.call.databinding.ActivityLoginBinding
import org.linphone.core.Call
import org.linphone.core.RegistrationState

class LoginActivity : AppCompatActivity(), LinphoneManager.Listener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var linphone: LinphoneManager
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        linphone = LinphoneManager.getInstance(this)

        requestRuntimePermissions()
        setupListeners()
    }

    private fun setupListeners() {
        binding.togglePassword.setOnClickListener { togglePasswordVisibility() }

        binding.forgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password? Contact Admin", Toast.LENGTH_SHORT).show()
        }

        binding.loginButton.setOnClickListener {
            performLogin()
        }

        // Press Enter to login
        binding.passwordInput.setOnEditorActionListener { _, _, _ ->
            performLogin()
            true
        }

        binding.signupButton.setOnClickListener {
            Toast.makeText(this, "Contact Admin for Sign Up", Toast.LENGTH_SHORT).show()
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

    private fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible

        val cursor = binding.passwordInput.selectionEnd
        binding.passwordInput.inputType =
            if (passwordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        binding.passwordInput.setSelection(cursor)

        binding.togglePassword.setImageResource(
            if (passwordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
        )
    }

    private fun performLogin() {
        val cnic = binding.cnicInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()

        // Validate CNIC
        if (cnic.isEmpty()) {
            binding.cnicInput.error = "CNIC is required"
            binding.cnicInput.requestFocus()
            return
        }
        if (cnic.length != 13) {
            binding.cnicInput.error = "CNIC must be 13 digits"
            binding.cnicInput.requestFocus()
            return
        }

        // Validate Password
        if (password.isEmpty()) {
            binding.passwordInput.error = "Password is required"
            binding.passwordInput.requestFocus()
            return
        }

        // Show loading
        setBusy(true, "Logging in...")

        // REGISTER TO SIP WITH HARDCODED CREDENTIALS
        registerToSip("1003", "Winter2023$$")
    }

    /**
     * Register to SIP server with hardcoded credentials
     */
    private fun registerToSip(sipId: String, password: String) {
        val domain = Config.SIP_DOMAIN

        // Register with Linphone
        linphone.register(
            sipId,      // "1003"
            password,   // "Winter2023$$"
            domain      // From Config
        )
    }

    override fun onRegistrationChanged(state: RegistrationState, message: String) {
        runOnUiThread {
            when (state) {
                RegistrationState.Progress -> {
                    setBusy(true, "Registering to SIP server...")
                }

                RegistrationState.Ok -> {
                    setBusy(false, "Login Successful!")

                    Toast.makeText(
                        this,
                        "Login Successful! / کامیابی سے لاگ ان ہو گیا!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to Dialer
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }

                RegistrationState.Failed -> {
                    setBusy(false, "Registration Failed")

                    Toast.makeText(
                        this,
                        "Registration Failed: $message",
                        Toast.LENGTH_LONG
                    ).show()
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

    override fun onCallStateChanged(call: Call, state: Call.State, message: String) {
        // Not used
    }

    private fun setBusy(busy: Boolean, status: String = "") {
        binding.loginButton.isEnabled = !busy
        binding.loginButton.alpha = if (busy) 0.6f else 1f
//        binding.loginButton.text = if (busy) status else "Login / لگ ان"
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
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, toRequest.toTypedArray(), 100)
        }
    }
}