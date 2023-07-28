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
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams

class Account : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        val navigateToLoginButton: Button = view.findViewById(R.id.account_login_btn)
        val navigateToAboutButton: Button = view.findViewById(R.id.account_about_btn)

        navigateToLoginButton.setOnClickListener {
            navigateToLogin()
        }

        navigateToAboutButton.setOnClickListener {
            navigateToAbout()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val usernameText: TextView = view.findViewById(R.id.username)
        val genSetting: TextView = view.findViewById(R.id.account_setting_text)
        val profilePic: ImageView = view.findViewById(R.id.profile_Pic)
        val layoutParams = genSetting.layoutParams as ConstraintLayout.LayoutParams

        if (currentUser != null) {
            navigateToLoginButton.visibility = View.GONE
            usernameText.visibility = View.VISIBLE
            profilePic.visibility = View.VISIBLE

            val username = "Welcome, ${currentUser.displayName}!"
            usernameText.text = username
            layoutParams.topToBottom = R.id.username
            genSetting.layoutParams = layoutParams
        } else {
            navigateToLoginButton.visibility = View.VISIBLE
            usernameText.visibility = View.GONE

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

    private fun navigateToAbout() {
        val intent = Intent(requireContext(), About::class.java)
        startActivity(intent)
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        navigateToMain()
    }
}
