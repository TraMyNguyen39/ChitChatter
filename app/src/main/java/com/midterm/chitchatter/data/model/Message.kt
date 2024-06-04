package com.midterm.chitchatter.data.model

import com.google.firebase.database.IgnoreExtraProperties
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
    var currentUserEmail: String

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
    val isIncoming: Boolean
        get() = (currentUserEmail ?: "").compareTo(sender) != 0
}