package com.example.bikebuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class EditProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        val backButton: AppCompatButton = findViewById(R.id.backedit)
        backButton.setOnClickListener {
            onBackPressed() // Handle the back button click to go back to the previous screen (fragment)
        }
    }

    override fun onBackPressed() {
        finish() // Finish the About activity to return to the previous fragment (Account fragment)
    }
}