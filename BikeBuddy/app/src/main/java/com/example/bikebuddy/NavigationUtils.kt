package com.example.bikebuddy

import android.content.Context
import android.content.Intent

object NavigationUtils {
    fun navigateToLoginScreen(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }
}

