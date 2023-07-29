package com.example.bikebuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.TextView
import android.widget.Toast
import com.example.bikebuddy.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()



        val navigateToForgotPassword: TextView = findViewById(R.id.login_forgot_password_btn)

        navigateToForgotPassword.setOnClickListener {
            navigateToForgotPassword()
        }

        val navigateToSignUpButton: TextView = findViewById(R.id.login_footer_btn)

        navigateToSignUpButton.setOnClickListener {
            navigateToSignup()
        }

        binding.loginPassRev.setOnClickListener {
            val currentTransformationMethod = binding.loginPasswordInput.transformationMethod

            if (currentTransformationMethod == HideReturnsTransformationMethod.getInstance()) {
                binding.loginPasswordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.loginPassRev.setImageResource(R.drawable.pass_rev)
            } else {
                binding.loginPasswordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.loginPassRev.setImageResource(R.drawable.pass_hide)
            }
            binding.loginPasswordInput.setSelection(binding.loginPasswordInput.text.length)
        }

        binding.loginBtn.setOnClickListener{
            val email = binding.loginUsernameInput.text.toString().trim()
            val password = binding.loginPasswordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Successfully Log In", Toast.LENGTH_SHORT).show()
                        loginFunc()
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun navigateToSignup() {
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToForgotPassword() {
        val intent = Intent(this, ForgotPassword::class.java)
        startActivity(intent)
    }


    private fun loginFunc() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


}
