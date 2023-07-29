package com.example.bikebuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton

class PrivacyPolicy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacypolicy)

        val backButton: AppCompatButton = findViewById(R.id.backPrivacy)
        backButton.setOnClickListener {
            onBackPressed() // Handle the back button click to go back to the previous screen (fragment)
        }
    }

    override fun onBackPressed() {
        finish() // Finish the About activity to return to the previous fragment (Account fragment)
    }
}