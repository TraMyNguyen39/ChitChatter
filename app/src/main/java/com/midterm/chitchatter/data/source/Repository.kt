package com.midterm.chitchatter.data.source

import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message

interface Repository {
    interface RemoteRepository : Repository {
        suspend fun createAccount(account: Account) : String
        suspend fun updateAccount(account: Account) : Boolean
        suspend fun login(account: Account) : Account?
        suspend fun sendResetPassword(email: String) : Int
        suspend fun sendEmailVerification(email: String) : Boolean
        suspend fun getAllLastMessages(email: String) : ArrayList<Message>
        suspend fun getChat(sender: String, receiver: String): List<Message>
        suspend fun sendMessage(message: Message): Boolean

    }

    interface LocalRepository : Repository {
        suspend fun insertAccount(account: Account)
        suspend fun deleteAccount(account: Account)
        suspend fun updateLocalAccount(account: Account)
        suspend fun getAccount(username: String): Account?
    }
}