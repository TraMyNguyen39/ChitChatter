package com.midterm.chitchatter.data.source

import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.DataSendMessage
import com.midterm.chitchatter.data.model.Message

interface Repository {
    interface RemoteRepository : Repository {
        suspend fun createAccount(account: Account) : String
        suspend fun updateAccount(account: Account) : Boolean
        suspend fun login(account: Account) : Account?
        suspend fun logout(account: Account) : Boolean
        suspend fun sendResetPassword(email: String) : Int
        suspend fun sendEmailVerification(email: String) : Boolean
        suspend fun getContactDetail(email: String) : Account?
        suspend fun getContactsOfAccount(email: String, token: String) : ArrayList<Account>
        suspend fun getAllLastMessages(email: String) : ArrayList<Message>
        suspend fun getChat(sender: String, receiver: String): List<Message>
        suspend fun sendMessage(message: Message): Boolean
        suspend fun sendMessage(message: DataSendMessage): Boolean
    }

    interface LocalRepository : Repository {
        // ToDo
    }
}