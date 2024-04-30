package com.midterm.chitchatter.data.source

import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message

interface Repository {
    interface RemoteRepository : Repository {
        suspend fun login(account: Account): Account?
        suspend fun createAccount(account: Account): String
        suspend fun updateAccount(account: Account): Boolean
        suspend fun loadFriendAccounts(username: String): List<Account>
        suspend fun sendMessage(message: Message): Boolean
        suspend fun getChat(sender: String, receiver: String): List<Message>
    }

    interface LocalRepository : Repository {
        suspend fun insertAccount(account: Account)
        suspend fun deleteAccount(account: Account)
        suspend fun updateLocalAccount(account: Account)
        suspend fun getAccount(username: String): Account?
    }
}