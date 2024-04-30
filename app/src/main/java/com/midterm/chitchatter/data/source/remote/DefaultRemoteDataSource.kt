package com.midterm.chitchatter.data.source.remote

import com.google.gson.GsonBuilder
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.source.DataSource
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DefaultRemoteDataSource : DataSource.RemoteDataSource {
    override suspend fun createAccount(account: Account): String {
        val baseUrl = "https://createaccountinfirestore-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.createAccount(account)
        return if (result.isSuccessful) {
            val responseObj = result.body()
            if (responseObj != null) {
                if (responseObj.success) {
                    "Success"
                } else {
                    responseObj.error!!
                }
            } else {
                "null"
            }
        } else {
            result.body()?.error!!
        }
    }
    override suspend fun getChat(sender: String, receiver: String): List<Message> {
        val baseUrl = "https://getchat-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.getChat(sender, receiver)
        if (result.isSuccessful) {
            return result.body() ?: emptyList()
        }
        return emptyList()
    }

    override suspend fun sendMessage(message: Message): Boolean {
        val baseUrl = "https://sendmessage-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.sendMessage(message)
        return result.isSuccessful

    }

    override suspend fun updateAccount(account: Account): Boolean {
        val baseUrl = "https://updateaccount-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.updateAccount(account)
        return result.isSuccessful
    }

    override suspend fun login(account: Account): Account? {
        val baseUrl = "https://login-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.login(account)
        if (result.isSuccessful) {
            return result.body()
        }
        return null
    }

    override suspend fun loadFriendAccounts(username: String): List<Account> {
        val baseUrl = "https://getallcontacts-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.getFriendAccounts(username)
        if (result.isSuccessful) {
            return result.body() ?: emptyList()
        }
        return emptyList()
    }

    private fun createRetrofitService(baseUrl: String): Retrofit {
        val gson = GsonBuilder().serializeNulls().create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}