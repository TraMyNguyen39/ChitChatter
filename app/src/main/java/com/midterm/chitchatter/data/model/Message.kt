package com.midterm.chitchatter.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
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

@Entity(tableName = "message")
data class Message @JvmOverloads constructor(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "",
    @ColumnInfo(name = "sender")
    val sender: String = "",
    @ColumnInfo(name = "receiver")
    val receiver: String = "",
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = Date().time,
    @ColumnInfo(name = "status")
    val status: Int = 0,
    @ColumnInfo(name = "token")
    val token: String? = null,
    @ColumnInfo(name = "name")
    val name: String = "",
//    val createdAt: String = Date().time.toString(),
    @ColumnInfo(name = "content")
    val content: String = "",
//    val isIncoming: Boolean = true,
    @ColumnInfo(name = "url")
    val url: String? = "",
    @ColumnInfo(name = "formattedTime")
    val formattedTime: String = "",
    @ColumnInfo(name = "photoUrl")
    val photoUrl: String? = null,
    @ColumnInfo(name = "photoMimeType")
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