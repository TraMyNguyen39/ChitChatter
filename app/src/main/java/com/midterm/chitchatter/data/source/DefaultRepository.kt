package com.midterm.chitchatter.data.source

import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message


class DefaultRepository(
    private val localDataSource: DataSource.LocalDataSource,
    private val remoteDataSource: DataSource.RemoteDataSource
) : Repository.RemoteRepository, Repository.LocalRepository {
    override suspend fun login(account: Account): Account? {
        return remoteDataSource.login(account)
    }

    override suspend fun createAccount(account: Account): String {
        return remoteDataSource.createAccount(account)
    }

    override suspend fun insertAccount(account: Account) {
        localDataSource.insertAccount(account)
    }

    override suspend fun deleteAccount(account: Account) {
        localDataSource.deleteAccount(account)
    }

    override suspend fun updateAccount(account: Account): Boolean {
        return remoteDataSource.updateAccount(account)
    }

    override suspend fun loadFriendAccounts(username: String): List<Account> {
        return remoteDataSource.loadFriendAccounts(username)
    }

    override suspend fun getAccount(username: String): Account? {
        return localDataSource.getSingleAccount(username)
    }

    override suspend fun updateLocalAccount(account: Account) {
        localDataSource.updateAccount(account)
    }

    override suspend fun sendMessage(message: Message): Boolean {
        return remoteDataSource.sendMessage(message)
    }

    override suspend fun getChat(sender: String, receiver: String): List<Message> {
        return remoteDataSource.getChat(sender, receiver)
    }
}