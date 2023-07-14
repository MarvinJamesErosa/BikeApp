package com.example.bikebuddy

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.PatternsCompat
import com.example.bikebuddy.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val navigateToLoginButton: TextView = findViewById(R.id.signup_footer_btn)
        navigateToLoginButton.setOnClickListener {
            navigateToLogin()
        }

        binding.signupCreateBtn.setOnClickListener {
            val email = binding.signupEmailInput.text.toString().trim()
            val password = binding.signupPasswordInput.text.toString()
            val confirmPass = binding.signupConfirmPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (password == confirmPass) {
                    if (isValidEmail(email)) {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = PatternsCompat.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }
}
