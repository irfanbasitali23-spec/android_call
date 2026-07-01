package com.esibil.call.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esibil.call.Config
import com.esibil.call.data.Prefs
import com.esibil.call.databinding.ActivityWelcomeBinding

/** First screen shown when the app is opened. */
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = Prefs(this)
        if (prefs.isLoggedIn) {
            Config.restoreFromSavedDomain(prefs.domain)
            startActivity(
                Intent(this, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
            return
        }

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddPrisoner.setOnClickListener {
            startActivity(Intent(this, BeforeContinueActivity::class.java))
        }
    }
}
