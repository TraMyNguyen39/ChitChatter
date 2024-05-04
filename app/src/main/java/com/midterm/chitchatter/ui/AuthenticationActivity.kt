package com.midterm.chitchatter.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.messaging.FirebaseMessaging
import com.midterm.chitchatter.R
import com.midterm.chitchatter.utils.ChitChatterUtils

class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        retrieveToken()
        var currentAccount = ChitChatterUtils.getCurrentAccount(this)
        if (currentAccount != null) {
            directToHome()
        }
    }

    private fun retrieveToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ChitChatterUtils.token = task.result
            }
        }
    }

    private fun directToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}