package com.example.bikebuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgetpass)

        val backButton: AppCompatButton = findViewById(R.id.backforgot)
        backButton.setOnClickListener {
            onBackPressed() // Handle the back button click to go back to the previous screen (fragment)
        }
    }

    override fun onBackPressed() {
        finish() // Finish the About activity to return to the previous fragment (Account fragment)
    }
}