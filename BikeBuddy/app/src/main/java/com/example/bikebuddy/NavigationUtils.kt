package com.example.bikebuddy

import android.content.Context
import android.content.Intent
import com.example.bikebuddy.LoginActivity

object NavigationUtils {
    fun navigateToLoginScreen(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }
}

