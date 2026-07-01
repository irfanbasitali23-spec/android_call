package com.esibil.call.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.esibil.call.Config
import com.esibil.call.core.LinphoneManager
import com.esibil.call.data.Prefs
import com.esibil.call.databinding.ActivityAddPrisonerBinding
import com.esibil.call.network.JailSite
import com.esibil.call.network.JailsApi
import com.esibil.call.service.SipForegroundService
import kotlinx.coroutines.launch
import org.linphone.core.Call
import org.linphone.core.RegistrationState
import java.io.File

/**
 * SIP account setup: pick a jail (sets the server IP), enter phone as username,
 * take 3 photos, then register and open [HomeActivity].
 */
class AddPrisonerActivity : AppCompatActivity(), LinphoneManager.Listener {

    private lateinit var binding: ActivityAddPrisonerBinding
    private lateinit var linphone: LinphoneManager
    private lateinit var prefs: Prefs

    private var jailSites: List<JailSite> = emptyList()
    private var selectedJail: JailSite? = null
    private var pendingPhotoIndex = 0
    private val photoFiles = arrayOfNulls<File>(3)
    private var pendingUsername = ""
    private var pendingDisplayName = ""
    private var pendingJail: JailSite? = null

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) showPhotoPreview(pendingPhotoIndex)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPrisonerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        linphone = LinphoneManager.getInstance(this)
        prefs = Prefs(this)

        binding.inputPassword.setText(Config.SIP_PASSWORD)
        requestRuntimePermissions()
        setupPhotoSlots()
        loadJails()

        binding.btnLogin.setOnClickListener { performLogin() }
    }

    override fun onResume() {
        super.onResume()
        linphone.addListener(this)
    }

    override fun onPause() {
        linphone.removeListener(this)
        super.onPause()
    }

    private fun loadJails() {
        binding.progressJails.visibility = View.VISIBLE
        binding.spinnerJail.isEnabled = false

        lifecycleScope.launch {
            try {
                jailSites = JailsApi.fetchSites()
                val names = listOf(getString(com.esibil.call.R.string.select_jail_hint)) +
                    jailSites.map { it.jailName }
                val adapter = ArrayAdapter(
                    this@AddPrisonerActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    names
                )
                binding.spinnerJail.adapter = adapter
                binding.spinnerJail.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedJail = if (position <= 0) null
                            else jailSites.getOrNull(position - 1)?.also {
                                Config.applyJailServer(it.ip)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            selectedJail = null
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AddPrisonerActivity,
                    "Failed to load jails: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressJails.visibility = View.GONE
                binding.spinnerJail.isEnabled = true
            }
        }
    }

    private fun setupPhotoSlots() {
        binding.photoSlot1.setOnClickListener { capturePhoto(0) }
        binding.photoSlot2.setOnClickListener { capturePhoto(1) }
        binding.photoSlot3.setOnClickListener { capturePhoto(2) }
    }

    private fun capturePhoto(index: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
            return
        }
        pendingPhotoIndex = index
        val file = File(filesDir, "prisoner_photo_$index.jpg")
        photoFiles[index] = file
        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            file
        )
        takePicture.launch(uri)
    }

    private fun showPhotoPreview(index: Int) {
        val file = photoFiles[index] ?: return
        if (!file.exists()) return
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return
        when (index) {
            0 -> binding.photoPreview1.setImageBitmap(bitmap)
            1 -> binding.photoPreview2.setImageBitmap(bitmap)
            2 -> binding.photoPreview3.setImageBitmap(bitmap)
        }
    }

    private fun performLogin() {
        val jail = selectedJail
        if (jail == null) {
            Toast.makeText(this, com.esibil.call.R.string.err_select_jail, Toast.LENGTH_SHORT).show()
            return
        }

        val username = binding.inputUsername.text?.toString()?.trim().orEmpty()
        if (username.isEmpty()) {
            binding.inputUsername.error = getString(com.esibil.call.R.string.err_username)
            binding.inputUsername.requestFocus()
            return
        }

        if (photoFiles.any { it == null || !it!!.exists() }) {
            Toast.makeText(this, com.esibil.call.R.string.err_photos, Toast.LENGTH_SHORT).show()
            return
        }

        val displayName = binding.inputDisplayName.text?.toString()?.trim()
            .takeUnless { it.isNullOrEmpty() }
            ?: username

        Config.applyJailServer(jail.ip)
        setBusy(true)

        pendingUsername = username
        pendingDisplayName = displayName
        pendingJail = jail

        linphone.register(username, Config.SIP_PASSWORD, Config.sipDomain)
    }

    override fun onRegistrationChanged(state: RegistrationState, message: String) {
        runOnUiThread {
            when (state) {
                RegistrationState.Progress -> setBusy(true)
                RegistrationState.Ok -> {
                    setBusy(false)
                    val jail = pendingJail ?: return@runOnUiThread
                    prefs.saveProfile(
                        phone = pendingUsername,
                        sipId = pendingUsername,
                        password = Config.SIP_PASSWORD,
                        domain = Config.sipDomain,
                        displayName = pendingDisplayName,
                        jailName = jail.jailName,
                        jailIp = jail.ip
                    )
                    photoFiles.forEachIndexed { i, file ->
                        prefs.setPhotoPath(i, file?.absolutePath)
                    }
                    SipForegroundService.start(this)
                    Toast.makeText(
                        this,
                        "Login Successful! / کامیابی سے لاگ ان ہو گیا!",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(
                        Intent(this, HomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                }
                RegistrationState.Failed -> {
                    setBusy(false)
                    Toast.makeText(
                        this,
                        "Registration Failed: $message",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> setBusy(false)
            }
        }
    }

    override fun onCallStateChanged(call: Call, state: Call.State, message: String) {}

    private fun setBusy(busy: Boolean) {
        binding.btnLogin.isEnabled = !busy
        binding.btnLogin.alpha = if (busy) 0.6f else 1f
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
