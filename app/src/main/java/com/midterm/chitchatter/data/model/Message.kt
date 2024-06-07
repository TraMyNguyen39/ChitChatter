package com.midterm.chitchatter.data.model

import com.google.firebase.database.IgnoreExtraProperties
import com.midterm.chitchatter.ui.home.HomeViewModel
import com.midterm.chitchatter.utils.ChitChatterUtils
import java.util.Date


enum class MessageStatus(val value: Int) {
    SENT(1),
    SEEN(2),
    SENDING(0),
    FAILED(-1),
    RECEIVED(3);

    fun toInt(): Int {
        return value
    }
}
data class DataSendMessage(
    val token: String? = null,
    val sender: String = "",
    val receiver: String = "",
    val content: String = "",
    val photoUrl: String? = null,
    val photoMimeType: String? = null
)

data class DataUpdateStatus(
    val id: String,
    val status: Int,
    val email: String? = "",
    val token: String? = ""
)

data class ResponseUpdateStatus(
    val id: String,
    val status: Int
)

@IgnoreExtraProperties
data class Data(
    val text: String = "",
    val photoUrl: String? = null,
    val photoMimeType: String? = null
)

data class Notification(
    val title: String = "",
    val body: String? = null
)

data class Message @JvmOverloads constructor(
    val id: String,
    val sender: String = "",
    val receiver: String = "",
    val data: Data,
    val notification: Notification,
    val timestamp: Long = Date().time,
    val status: Int,
    val token: String? = null,
    val name: String = "",
//    val createdAt: String = Date().time.toString(),
    val content: String = "",
//    val isIncoming: Boolean = true,
    val url: String = "",
    val formattedTime: String,
    val photoUrl: String? = null,
    val photoMimeType: String? = null

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (sender != other.sender) return false
        if (receiver != other.receiver) return false
        return formattedTime == other.formattedTime
    }

    override fun hashCode(): Int {
        var result = sender.hashCode()
        result = 31 * result + receiver.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
    var currentUserEmail: String = ""
        get() = ChitChatterUtils.currentAccountEmail ?: ""
    val isIncoming: Boolean
        get() = (currentUserEmail ?: "").compareTo(sender) != 0
}