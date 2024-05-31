package com.midterm.chitchatter.utils


import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.ui.chat.ChatFragment
import com.midterm.chitchatter.ui.chat.ChatViewModel


class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FirebaseMessagingService", "Message data payload: ${remoteMessage.data}")

            val senderEmail = remoteMessage.data["senderEmail"]
            val message = remoteMessage.data["message"]
            showNotification(senderEmail, message)
        }
    }

    private fun showNotification(senderEmail: String?, message: String?) {
        val channelId = "channel_id"
        val notificationId = 1

        val intent = Intent(this, ChatFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Xây dựng thông báo
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.chitchatter_logo)
            .setContentTitle(senderEmail)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Tự động hủy thông báo khi người dùng nhấn vào nó

        // Hiển thị thông báo
        if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED) {
            Log.d("FirebaseMessagingService", "Sending notification")
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        } else {
            Log.d("FirebaseMessagingService", "Permission not granted")
        }
    }
}