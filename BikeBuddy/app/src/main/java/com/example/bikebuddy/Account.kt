package com.example.bikebuddy

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class Account : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        val navigateToLoginButton: Button = view.findViewById(R.id.navigateToLoginButton)

        navigateToLoginButton.setOnClickListener {
            // Handle the button click event
            navigateToLogin()
        }

        return view
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }
}
