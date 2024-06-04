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
import com.google.firebase.database.DataSnapshot
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Data
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.model.MessageStatus
import com.midterm.chitchatter.data.model.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChitChatterUtils {
    val email: String? = null
    var token: String? = null
    var currentAccountEmail: String? = null


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

    fun convertSnapshotToMessage(snapshot: DataSnapshot, currentUserEmail: String): Message {
        // Extract values from snapshot
        val key = snapshot.key ?: ""
        val value = snapshot.value as? Map<*, *> ?: emptyMap<Any, Any>()
        val id = value["id"] as? String ?: ""
        val createdAtStr = value["createdAt"] as? String ?: ""
        val sender = value["sender"] as? String ?: ""
        val receiver = value["receiver"] as? String ?: ""
        val content = value["content"] as? String ?: ""
        val statusInt = (value["status"] as? Long)?.toInt() ?: MessageStatus.SENDING.toInt()

        // Parse createdAt to timestamp (milliseconds)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val createdAt: Long = try {
            val date: Date = dateFormat.parse(createdAtStr) ?: Date()
            date.time
        } catch (e: Exception) {
            Date().time
        }

        val formattedTime = value["formattedTime"] as? String ?: ""

        // Create Data and Notification objects with default values (or based on snapshot data if needed)
        val data = Data(text = content)
        val notification = Notification(title = "", body = content)

        // Construct the Message object
        val message = Message(
            id = id,
            sender = sender,
            receiver = receiver,
            data = data,
            notification = notification,
            timestamp = createdAt,
            status = statusInt,
            token = null,
            name = "",
            content = content,
            url = "",
            formattedTime = formattedTime
        )

        return message
    }

    fun saveTokenToSharedPreferences(context: Context, token: String) {
        val accountKey = context.getString(R.string.preference_account_key)
        val tokenKey = context.getString(R.string.preference_token_key)

        val sharedPref: SharedPreferences = context.getSharedPreferences(accountKey, Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString(tokenKey, token)
            apply()
        }
    }
    fun getCurrentAccountToken(context: Context): String? {
        var account_key = context.getString(R.string.preference_account_key);
        var tokenKey = context.getString(R.string.preference_token_key);

        val sharedPref: SharedPreferences = context.getSharedPreferences(account_key, Context.MODE_PRIVATE)
        val token: String? = sharedPref.getString(tokenKey, null)

        return token
    }
    fun setCurrentAccountToken(context: Context) {
        var account_key = context.getString(R.string.preference_account_key);
        var tokenKey = context.getString(R.string.preference_token_key);

        val sharedPref: SharedPreferences = context.getSharedPreferences(account_key, Context.MODE_PRIVATE)
        token = sharedPref.getString(tokenKey, null)
    }
    fun setCurrentAccountEmail(context: Context) {
        var account_key = context.getString(R.string.preference_account_key);
        var emailKey = context.getString(R.string.preference_email_key);

        val sharedPref: SharedPreferences = context.getSharedPreferences(account_key, Context.MODE_PRIVATE)
        currentAccountEmail = sharedPref.getString(emailKey, null)
    }
}