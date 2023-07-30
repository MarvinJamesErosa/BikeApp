package com.example.bikebuddy

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class EditProfile : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!

        val backButton: AppCompatButton = findViewById(R.id.backedit)
        backButton.setOnClickListener {
            onBackPressed()
        }

        val saveButton: AppCompatButton = findViewById(R.id.saveBtn)
        saveButton.setOnClickListener {
            val fullName: String? = findViewById<TextView>(R.id.usernametextview).text.toString()
            val newEmail: String? = findViewById<TextView>(R.id.emailtextview).text.toString().trim()

            // Update display name
            if (!fullName.isNullOrEmpty()) {
                val newDisplayName = fullName
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newDisplayName)
                    .build()

                currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@EditProfile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@EditProfile, "Failed to update profile", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {

            }

            // Update email
            if (!newEmail.isNullOrEmpty()) {
                currentUser.updateEmail(newEmail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Email updated successfully
                            Toast.makeText(this@EditProfile, "Email updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            // Failed to update email
                            Toast.makeText(this@EditProfile, "Failed to update email", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle the exception here
                        Toast.makeText(this@EditProfile, "Email update failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {

            }
        }
    }


    override fun onBackPressed() {
        finish()
    }
}