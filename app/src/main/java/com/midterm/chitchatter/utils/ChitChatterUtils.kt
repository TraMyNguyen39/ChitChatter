package com.midterm.chitchatter.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.textfield.TextInputEditText
import com.midterm.chitchatter.R

object ChitChatterUtils {
    var token: String? = null

    fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                afterTextChanged.invoke(editableText.toString())
            }
        })
    }
    fun isOnline(context: Context): Boolean {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
    fun hideKeyBoard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // on below line hiding our keyboard.
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getCurrentAccount(context: Context): String? {
        var account_key = context.getString(R.string.preference_account_key);
        var emailKey = context.getString(R.string.preference_email_key);

        val sharedPref: SharedPreferences = context.getSharedPreferences(account_key, Context.MODE_PRIVATE)
        val email: String? = sharedPref.getString(emailKey, null)

        return email
    }
}