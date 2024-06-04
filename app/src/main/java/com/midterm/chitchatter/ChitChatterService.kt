package com.midterm.chitchatter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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

//    override fun onMessageReceived(message: RemoteMessage) {
//        super.onMessageReceived(message)
//        _remoteMessage.postValue(message)
//    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Xử lý thông báo FCM đến.
        remoteMessage.notification?.let {
            // Hiển thị thông báo
            val data = remoteMessage.data
            sendNotification(it.title, it.body, data["sender"], data["createdAt"])
        }
    }

    private fun sendNotification(title: String?, messageBody: String?, sender: String?, createdAt: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "default_channel"
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("$messageBody\nFrom: $sender\nAt: $createdAt")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

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