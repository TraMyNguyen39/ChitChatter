package com.midterm.chitchatter.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.midterm.chitchatter.R
import com.midterm.chitchatter.utils.ChitChatterUtils

class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        retrieveToken()

        val currentEmail = ChitChatterUtils.getCurrentAccount(this)
        if (currentEmail != null) {
            directToHome()
        }
    }

    private fun retrieveToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ChitChatterUtils.token = task.result
                Log.e("TOKEN", ChitChatterUtils.token!!)
            }
        }
    }

    private fun directToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}