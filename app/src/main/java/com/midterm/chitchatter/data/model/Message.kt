package com.midterm.chitchatter.data.model

import com.midterm.chitchatter.ui.home.HomeViewModel
import java.util.Date

enum class MessageStatus {
    SENT,
    SEEN,
    SENDING,
    FAILED,
    RECEIVED
}

data class Data(
    val text: String = "",
    val photoUrl: String? = null,
    val photoMimeType: String? = null
)

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
    val status: MessageStatus = MessageStatus.SENT,
    val token: String? = null,
    val name: String = "",
    val createdAt: String = Date().time.toString(),
    val content: String = "",
    val isIncoming: Boolean = false,
    val url: String = ""

    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (sender != other.sender) return false
        if (receiver != other.receiver) return false
        return createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = sender.hashCode()
        result = 31 * result + receiver.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
//    val isIncoming: Boolean
//        get() = HomeViewModel.getCurrentAccount()?.compareTo(sender) != 0
}
