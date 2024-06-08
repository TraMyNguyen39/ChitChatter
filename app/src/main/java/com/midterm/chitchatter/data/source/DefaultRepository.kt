package com.midterm.chitchatter.data.source

import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.ContactRequestSender
import com.midterm.chitchatter.data.model.DataSendMessage
import com.midterm.chitchatter.data.model.DataUpdateStatus
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.source.local.DefaultLocalDataSource
import com.midterm.chitchatter.data.source.remote.DefaultRemoteDataSource
import com.midterm.chitchatter.data.source.remote.ResponseResult

class DefaultRepository(
    private val remoteDataSource: DefaultRemoteDataSource,
    private val localDataSource: DefaultLocalDataSource
) : Repository.RemoteRepository, Repository.LocalRepository {
    override suspend fun createAccount(account: Account): String {
        return remoteDataSource.createAccount(account)
    }

    override suspend fun updateAccount(account: Account): Boolean {
        return remoteDataSource.updateAccount(account)
    }

    override suspend fun updateAvatar(account: Account): Boolean {
        return remoteDataSource.updateAvatar(account)
    }

    override suspend fun login(account: Account): Account? {
        return remoteDataSource.login(account)
    }
    override suspend fun logout(account: Account): Boolean {
        return remoteDataSource.logout(account)
    }
    override suspend fun sendResetPassword(email: String): Int {
        return remoteDataSource.sendResetPassword(email)
    }

    override suspend fun sendEmailVerification(email: String): Boolean {
        return remoteDataSource.sendEmailVerification(email)
    }

    override suspend fun getContactDetail(email: String): Account? {
        return remoteDataSource.getContactDetail(email)
    }

    override suspend fun getContactDetailConnection(userEmail: String, contactEmail: String, token: String): Account? {
        return remoteDataSource.getContactDetailConnection(userEmail, contactEmail, token)
    }

    override suspend fun addContact(
        userEmail: String,
        contactEmail: String,
        token: String
    ): Boolean {
        return remoteDataSource.addContact(userEmail, contactEmail, token)
    }

    override suspend fun deleteContact(
        userEmail: String,
        contactEmail: String,
        token: String
    ): Boolean {
        return remoteDataSource.deleteContact(userEmail, contactEmail, token)
    }

    override suspend fun acceptContact(
        userEmail: String,
        contactEmail: String,
        token: String
    ): Boolean {
        return remoteDataSource.acceptContact(userEmail, contactEmail, token)
    }

    override suspend fun rejectContact(
        userEmail: String,
        contactEmail: String,
        token: String
    ): Boolean {
        return remoteDataSource.rejectContact(userEmail, contactEmail, token)
    }

    override suspend fun getContactsOfAccount(email: String, token: String): ArrayList<Account> {
        return remoteDataSource.getContactsOfAccount(email, token)
    }

    override suspend fun getContactRequests(
        email: String,
        token: String
    ): ArrayList<ContactRequestSender> {
        return remoteDataSource.getContactRequests(email, token)
    }

    override suspend fun countUnreadNotifications(email: String, token: String): Int {
        return remoteDataSource.countUnreadNotifications(email, token)
    }

    override suspend fun markAllAsRead(email: String, token: String): Boolean {
        return remoteDataSource.markAllAsRead(email, token)
    }

    override suspend fun removeContactRequestFromRealtimeDB(email: String, token: String): Boolean {
        return remoteDataSource.removeContactRequestFromRealtimeDB(email, token)
    }

    override suspend fun getContactsSearch(textSearch: String, email: String,token: String): ArrayList<Account> {
        return remoteDataSource.getContactsSearch(textSearch, email, token)
    }

    override suspend fun getAllLastMessages(email: String): ArrayList<Message> {
        return remoteDataSource.getAllLastMessages(email)
    }
    override suspend fun getChat(sender: String, receiver: String): List<Message> {
        return remoteDataSource.getChat(sender, receiver)
    }
    override suspend fun sendMessage(message: Message): Boolean {
        return remoteDataSource.sendMessage(message)
    }
    override suspend fun sendMessage(message: DataSendMessage): Boolean {
        return remoteDataSource.sendMessage(message)
    }

    override suspend fun updateMessageStatus(
        data: DataUpdateStatus
    ): Boolean {
        return remoteDataSource.updateMessageStatus(data)
    }

    override suspend fun loadData(): ArrayList<Message> {
        return localDataSource.loadData()
    }

    override suspend fun clearDatabase(): Boolean {
        return localDataSource.clearDatabase()
    }

    override suspend fun updateDatabase(messages: List<Message>): Boolean {
        return localDataSource.updateDatabase(messages)
    }

    suspend fun loadDataWithNetworkCheck(hasNetwork: Boolean, email: String): ArrayList<Message> {
        return if (hasNetwork) {
            remoteDataSource.getAllLastMessages(email)
        } else {
            localDataSource.loadData()
        }
    }
}