package com.midterm.chitchatter.utils


import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.ui.chat.ChatViewModel


class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FirebaseMessagingService", "Message data payload: ${remoteMessage.data}")

            val senderEmail = remoteMessage.data["senderEmail"]
            val message = remoteMessage.data["message"]
            updateNewMessage()
        }
    }

    fun updateNewMessage() {
//        val chatViewModel = ChatViewModel(repository = Repository.RemoteRepository())
////        chatViewModel.updateMessages(senderEmail, message)
//        chatViewModel.updateMessages()

    }
}