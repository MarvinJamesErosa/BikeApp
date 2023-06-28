package com.example.bikebuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.TextView

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val navigateToSignUpButton: TextView = findViewById(R.id.login_footer_btn)

        navigateToSignUpButton.setOnClickListener {
            navigateToSignup()
        }
    }

    private fun navigateToSignup() {
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
    }
}
