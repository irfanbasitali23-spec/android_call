package com.esibil.call.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esibil.call.databinding.ActivityWelcomeBinding

/** First screen shown when the app is opened. */
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddPrisoner.setOnClickListener {
            startActivity(Intent(this, BeforeContinueActivity::class.java))
        }
    }
}
