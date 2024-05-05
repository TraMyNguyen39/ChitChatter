package com.midterm.chitchatter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ChitChatterService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("TOKEN", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        _remoteMessage.postValue(message)
    }
    companion object {
        private val _remoteMessage = MutableLiveData<RemoteMessage>()
        val remoteMessage: LiveData<RemoteMessage> = _remoteMessage
    }
}