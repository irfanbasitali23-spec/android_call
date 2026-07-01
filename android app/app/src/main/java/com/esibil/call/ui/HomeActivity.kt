package com.esibil.call.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.esibil.call.R
import com.esibil.call.core.LinphoneManager
import com.esibil.call.data.Prefs
import com.esibil.call.databinding.ActivityHomeBinding
import com.esibil.call.service.SipForegroundService
import org.linphone.core.Call
import org.linphone.core.RegistrationState

/**
 * Main shell after login. Hosts Home and Profile bottom-nav tabs and listens
 * for incoming SIP calls.
 */
class HomeActivity : AppCompatActivity(), LinphoneManager.Listener, HomeHost {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var linphone: LinphoneManager

    private var currentTab = HomeTab.HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        linphone = LinphoneManager.getInstance(this)

        if (savedInstanceState == null) {
            showTab(HomeTab.HOME, addFragments = true)
        } else {
            currentTab = HomeTab.entries[savedInstanceState.getInt(KEY_TAB, 0)]
            updateNavUi(currentTab)
        }

        binding.navHome.setOnClickListener { switchTab(HomeTab.HOME) }
        binding.navProfile.setOnClickListener { switchTab(HomeTab.PROFILE) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TAB, currentTab.ordinal)
    }

    override fun onResume() {
        super.onResume()
        linphone.addListener(this)
    }

    override fun onPause() {
        linphone.removeListener(this)
        super.onPause()
    }

    override fun switchTab(tab: HomeTab) {
        if (tab == currentTab && supportFragmentManager.findFragmentById(R.id.fragmentContainer) != null) {
            return
        }
        showTab(tab, addFragments = false)
    }

    override fun logout() {
        linphone.unregister()
        Prefs(this).clear()
        stopService(Intent(this, SipForegroundService::class.java))
        startActivity(
            Intent(this, WelcomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    private fun showTab(tab: HomeTab, addFragments: Boolean) {
        currentTab = tab
        val fragment = fragmentFor(tab)
        val tx = supportFragmentManager.beginTransaction()
        if (addFragments) {
            tx.add(R.id.fragmentContainer, fragment, tagFor(tab))
        } else {
            supportFragmentManager.fragments
                .filter { it.isVisible }
                .forEach { tx.hide(it) }
            val existing = supportFragmentManager.findFragmentByTag(tagFor(tab))
            if (existing != null) {
                tx.show(existing)
            } else {
                tx.add(R.id.fragmentContainer, fragment, tagFor(tab))
            }
        }
        tx.commit()
        updateNavUi(tab)
    }

    private fun fragmentFor(tab: HomeTab): Fragment = when (tab) {
        HomeTab.HOME -> HomeFragment()
        HomeTab.PROFILE -> ProfileFragment()
    }

    private fun tagFor(tab: HomeTab): String = when (tab) {
        HomeTab.HOME -> TAG_HOME
        HomeTab.PROFILE -> TAG_PROFILE
    }

    private fun updateNavUi(tab: HomeTab) {
        val active = ContextCompat.getColor(this, R.color.bottom_nav_active)
        val inactive = ContextCompat.getColor(this, R.color.bottom_nav_inactive)

        styleNav(binding.navHomeIcon, binding.navHomeLabel, tab == HomeTab.HOME, active, inactive)
        styleNav(binding.navProfileIcon, binding.navProfileLabel, tab == HomeTab.PROFILE, active, inactive)

        binding.navHomeIcon.setImageResource(R.drawable.ic_home_filled)
        binding.navProfileIcon.setImageResource(
            if (tab == HomeTab.PROFILE) R.drawable.ic_person_green else R.drawable.ic_person_outline_gray
        )
    }

    private fun styleNav(
        icon: ImageView,
        label: TextView,
        selected: Boolean,
        active: Int,
        inactive: Int
    ) {
        label.setTextColor(if (selected) active else inactive)
        label.setTypeface(null, if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        icon.alpha = if (selected) 1f else 0.7f
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

    companion object {
        private const val TAG_HOME = "tab_home"
        private const val TAG_PROFILE = "tab_profile"
        private const val KEY_TAB = "current_tab"
    }
}
