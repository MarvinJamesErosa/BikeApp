package com.example.bikebuddy

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import kotlin.math.roundToInt
import androidx.core.content.ContextCompat

class Account : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        val topButton: Button = view.findViewById(R.id.account_login_btn)
        val navigateToAboutButton: Button = view.findViewById(R.id.account_about_btn)
        val navigateToTermsConditions: Button = view.findViewById(R.id.account_terms_btn)
        val navigateToEditProfile: Button = view.findViewById(R.id.account_login_btn)
        val navigateToPrivacyPolicy: Button = view.findViewById(R.id.account_privacy_btn)
        val verifyBtn: TextView = view.findViewById(R.id.verify_now_btn)
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val usernameText: TextView = view.findViewById(R.id.username)
        val profilePic: ImageView = view.findViewById(R.id.profile_Pic)
        val emailText: TextView = view.findViewById(R.id.email_text)
        val logoutBtn: Button = view.findViewById(R.id.account_logout_btn)
        val topButtonParams = topButton.layoutParams as LayoutParams


        navigateToEditProfile.setOnClickListener{
            navigateToEditProfile()
        }

        navigateToAboutButton.setOnClickListener {
            navigateToAbout()
        }

        navigateToTermsConditions.setOnClickListener {
            navigateToTermsConditions()
        }


        navigateToPrivacyPolicy.setOnClickListener {
            navigateToPrivacyPolicy()
        }


        verifyBtn.setOnClickListener{
            currentUser?.sendEmailVerification()?.addOnSuccessListener {
                Toast.makeText(requireContext(), "Sent Verification Email!", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                navigateToLogin()
                Toast.makeText(requireContext(), "Please Log In Again", Toast.LENGTH_SHORT).show()
            }?.addOnFailureListener {exception ->
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }
        }



        if (currentUser != null) {

            usernameText.visibility = View.VISIBLE
            profilePic.visibility = View.VISIBLE
            logoutBtn.visibility = View.VISIBLE
            emailText.visibility = View.VISIBLE

            val username = currentUser.displayName
            val email = currentUser.email

            usernameText.text = username
            emailText.text = email

            val marginInDp = 15f
            val scale = resources.displayMetrics.density
            val marginInPixels = (marginInDp * scale).roundToInt()

            topButtonParams.topToBottom = R.id.verify_now_btn
            topButtonParams.topMargin = marginInPixels
            topButton.text = "Edit Profile"
            topButton.layoutParams = topButtonParams

            val editIcon = ContextCompat.getDrawable(requireContext(), R.drawable.edit_icon)

            topButton.setCompoundDrawablesWithIntrinsicBounds(editIcon, null,null, null)


            val isEmailVerified = currentUser.isEmailVerified
            if (isEmailVerified) {

                verifyBtn.visibility = View.GONE
                val marginInDp = 15f
                val scale = resources.displayMetrics.density
                val marginInPixels = (marginInDp * scale).roundToInt()

                topButtonParams.topToBottom = R.id.email_text
                topButtonParams.topMargin = marginInPixels
                topButton.layoutParams = topButtonParams


            } else {
                verifyBtn.visibility = View.VISIBLE
            }

        } else {
            topButton.text = "Login"
            topButton.setOnClickListener {
                navigateToLogin()
            }

        }

        val logoutButton: Button = view.findViewById(R.id.account_logout_btn)

        logoutButton.setOnClickListener {
            logout()
        }

        return view
    }

    private fun navigateToLogin() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToTermsConditions() {
        val intent = Intent(requireActivity(), TermsConditions::class.java)
        startActivity(intent)
    }

    private fun navigateToPrivacyPolicy() {
        val intent = Intent(requireContext(), PrivacyPolicy::class.java)
        startActivity(intent)
    }

    private fun navigateToAbout() {
        val intent = Intent(requireContext(), About::class.java)
        startActivity(intent)
    }


    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToEditProfile() {
        val intent = Intent(requireContext(), EditProfile::class.java)
        startActivity(intent)
    }


    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        navigateToMain()
    }


}
