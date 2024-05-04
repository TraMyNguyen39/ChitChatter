package com.midterm.chitchatter.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.ui.login.LoginViewModel
import com.midterm.chitchatter.ui.login.LoginViewModelFactory
import com.midterm.chitchatter.utils.ChitChatterUtils

class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        retrieveToken()
        getCurrentAccount()
    }
    private fun retrieveToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ChitChatterUtils.token = task.result
            }
        }
    }
    private fun getCurrentAccount() {
        val sharedPref = getSharedPreferences(
            getString(R.string.preference_account_key), Context.MODE_PRIVATE)

        val email = sharedPref.getString(getString(R.string.preference_email_key), null)
        if (email != null) {
            directToHome()
        }

//        val editor = sharedPref.edit()
//        editor.clear() // Xóa toàn bộ dữ liệu
//        editor.apply() // Áp dụng thay đổi
    }

    private fun directToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}