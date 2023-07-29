package com.example.bikebuddy

import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgetpass)

        val backButton: AppCompatButton = findViewById(R.id.backforgot)
        backButton.setOnClickListener {
            onBackPressed() // Handle the back button click to go back to the previous screen (fragment)
        }

        val emailInput: TextView = findViewById(R.id.textviewforgot)
        val resetPasswordButton: Button = findViewById(R.id.resetPassBtn)
        resetPasswordButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            sendPasswordResetEmail(email)
        }
    }

    fun sendPasswordResetEmail(email: String) {
        val auth = FirebaseAuth.getInstance()

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, "Sent Email for Reset Password Link!", Toast.LENGTH_SHORT).show()
                onBackPressed()

            }?.addOnFailureListener {exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
            }
    }



    override fun onBackPressed() {
        finish() // Finish the About activity to return to the previous fragment (Account fragment)
    }
}