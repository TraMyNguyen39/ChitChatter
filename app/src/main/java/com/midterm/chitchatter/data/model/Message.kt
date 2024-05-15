package com.midterm.chitchatter.data.model

import com.midterm.chitchatter.ui.home.HomeViewModel
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


data class Notification(
    val title: String = "",
    val body: String? = null
)

data class Message(
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
    val url: String = "",
    val formattedTime: String,
    val currentUserEmail: String

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
        result = 31 * result + createdAt.hashCode()
        return result
    }
    val isIncoming: Boolean
        get() = (currentUserEmail ?: "").compareTo(sender) != 0
}