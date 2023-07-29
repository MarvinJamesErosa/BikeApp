package com.example.bikebuddy

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.PatternsCompat
import com.example.bikebuddy.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
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

        binding.signupPassRev.setOnClickListener {
            val currentTransformationMethod = binding.signupPasswordInput.transformationMethod

            if (currentTransformationMethod == HideReturnsTransformationMethod.getInstance()) {
                binding.signupPasswordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.signupPassRev.setImageResource(R.drawable.pass_rev)
            } else {
                binding.signupPasswordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.signupPassRev.setImageResource(R.drawable.pass_hide)
            }
            binding.signupPasswordInput.setSelection(binding.signupPasswordInput.text.length)
        }

        binding.signupConPassRev.setOnClickListener {
            val currentTransformationMethod = binding.signupConfirmPassword.transformationMethod

            if (currentTransformationMethod == HideReturnsTransformationMethod.getInstance()) {
                binding.signupConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.signupConPassRev.setImageResource(R.drawable.pass_rev)
            } else {
                binding.signupConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.signupConPassRev.setImageResource(R.drawable.pass_hide)
            }
            binding.signupConfirmPassword.setSelection(binding.signupConfirmPassword.text.length)
        }

        binding.signupCreateBtn.setOnClickListener {
            val username = binding.signupUsernameInput.text.toString().trim()
            val email = binding.signupEmailInput.text.toString().trim()
            val password = binding.signupPasswordInput.text.toString()
            val confirmPass = binding.signupConfirmPassword.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (password == confirmPass) {
                    if (isValidEmail(email)) {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user: FirebaseUser? = firebaseAuth.currentUser

                                    user?.sendEmailVerification()?.addOnSuccessListener {
                                        Toast.makeText(this, "Sent Verification Email!", Toast.LENGTH_SHORT).show()
                                    }?.addOnFailureListener {
                                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                                    }

                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build()
                                    user?.updateProfile(profileUpdates)
                                        ?.addOnCompleteListener { profileTask ->
                                            if (profileTask.isSuccessful) {
                                                val intent = Intent(this, LoginActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Toast.makeText(this, "Failed to update user profile", Toast.LENGTH_SHORT).show()
                                            }
                                        }
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
        finish()
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = PatternsCompat.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun emailAuth(){

    }
}
