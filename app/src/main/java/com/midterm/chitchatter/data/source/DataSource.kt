package com.midterm.chitchatter.data.source

import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message

interface DataSource {
    interface RemoteDataSource {
        suspend fun createAccount(account: Account): String
        suspend fun updateAccount(account: Account): Boolean
        suspend fun login(account: Account): Account?
        suspend fun loadFriendAccounts(username: String): List<Account>
        suspend fun sendMessage(message: Message): Boolean
        suspend fun getChat(sender: String, receiver: String): List<Message>
    }

    interface LocalDataSource {
        suspend fun insertAccount(account: Account)
        suspend fun updateAccount(account: Account)
        suspend fun deleteAccount(account: Account)
        suspend fun getSingleAccount(username: String): Account?
    }
}