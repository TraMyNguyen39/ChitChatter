package com.midterm.chitchatter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.midterm.chitchatter.ui.MainActivity

class ChitChatterService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
//        Log.e("TOKEN", token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Xử lý thông báo FCM đến.
        Log.e("RECEIVED", "Vao 0")
        if (remoteMessage.data.isNotEmpty()) {
            Log.i("RECEIVED", "Vao 1")
            // Hiển thị thông báo
            val data = remoteMessage.data
            Log.i("RECEIVED", "Vao 2")
            if (data["title"] == "Yêu cầu kết bạn") {
                sendFriendRequestNotification(data["title"], data["body"])
                Log.i("RECEIVED", "Vao 2")
            } else {
//                sendNotification(it.title, it.body, data["sender"], data["createdAt"])
                Log.i("RECEIVED", "Vao 4")
            }
        }
    }

    private fun sendFriendRequestNotification(title: String?, body: String?) {
        // Intent for the "View Detail" action
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("navigation", MainActivity.NAVIGATE_TO_CONTACT_REQUEST)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 100, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        // Intent for the "Cancel" action
        val cancelIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "CANCEL_NOTIFICATION"
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(this, 101, cancelIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "com.midterm.chitchatter.contact-request-notification"
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_user, "View Details", pendingIntent)
            .addAction(R.drawable.ic_remove, "Cancel", cancelPendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Tạo notification channel cho Android Oreo trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun sendNotification(title: String?, messageBody: String?, sender: String?, createdAt: String?) {
        // Intent for the "View Detail" action
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 104, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        // Intent for the "Cancel" action
        val cancelIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = "CANCEL_NOTIFICATION"
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(this, 103, cancelIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "com.midterm.chitchatter.chat-notification"
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText("$messageBody\nFrom: $sender\nAt: $createdAt")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_chat, "View Details", pendingIntent)
            .addAction(R.drawable.ic_remove, "Cancel", cancelPendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Tạo notification channel cho Android Oreo trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private val _remoteMessage = MutableLiveData<RemoteMessage>()
        val remoteMessage: LiveData<RemoteMessage> = _remoteMessage
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "CANCEL_NOTIFICATION") {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(0)
        }
    }
}
