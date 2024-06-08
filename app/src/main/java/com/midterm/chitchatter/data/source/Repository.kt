package com.midterm.chitchatter.data.source

import android.content.Context
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.ContactRequestSender
import com.midterm.chitchatter.data.model.DataSendMessage
import com.midterm.chitchatter.data.model.DataUpdateStatus
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.source.remote.ResponseResult
import com.midterm.chitchatter.utils.ChitChatterUtils

interface Repository {
    interface RemoteRepository : Repository {
        suspend fun createAccount(account: Account) : String
        suspend fun updateAccount(account: Account) : Boolean
        suspend fun updateAvatar(account: Account) : Boolean
        suspend fun login(account: Account) : Account?
        suspend fun logout(account: Account) : Boolean
        suspend fun sendResetPassword(email: String) : Int
        suspend fun sendEmailVerification(email: String) : Boolean
        suspend fun getContactDetail(email: String) : Account?
        suspend fun getContactDetailConnection(
            userEmail: String,
            contactEmail: String,
            token: String
        ): Account?
        suspend fun addContact(userEmail: String, contactEmail: String, token: String) : Boolean
        suspend fun deleteContact(userEmail: String, contactEmail: String, token: String) : Boolean
        suspend fun acceptContact(userEmail: String, contactEmail: String, token: String) : Boolean
        suspend fun rejectContact(userEmail: String, contactEmail: String, token: String) : Boolean
        suspend fun getContactsOfAccount(email: String, token: String) : ArrayList<Account>
        suspend fun getContactRequests(email: String, token: String): ArrayList<ContactRequestSender>
        suspend fun countUnreadNotifications(email: String, token: String): Int
        suspend fun markAllAsRead(email: String, token: String): Boolean
        suspend fun removeContactRequestFromRealtimeDB(email: String, token: String): Boolean
        suspend fun getContactsSearch(textSearch: String, email: String, token: String) : ArrayList<Account>
        suspend fun getAllLastMessages(email: String) : ArrayList<Message>
        suspend fun getChat(sender: String, receiver: String): List<Message>
        suspend fun sendMessage(message: Message): Boolean
        suspend fun sendMessage(message: DataSendMessage): Boolean
        suspend fun updateMessageStatus(data: DataUpdateStatus): Boolean
    }

    interface LocalRepository : Repository {
        suspend fun loadData(): ArrayList<Message>
        suspend fun clearDatabase(): Boolean

        suspend fun updateDatabase(messages: List<Message>): Boolean
    }
}