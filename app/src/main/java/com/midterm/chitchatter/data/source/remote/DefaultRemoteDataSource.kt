package com.midterm.chitchatter.data.source.remote

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.source.DataSource
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.midterm.chitchatter.data.source.remote.ResponseResult

class DefaultRemoteDataSource : DataSource.RemoteDataSource {
    override suspend fun createAccount(account: Account): String {
        val baseUrl = "https://createaccountinfirestore-kz4isf6rva-uc.a.run.app"
        val auth = Firebase.auth
        try {
            // Tạo tài khoản authentication: không làm được ở admin
            auth.createUserWithEmailAndPassword(account.email, account.password!!).await()
            account.password = null
            account.isVerified = null
            // Tạo account trong firebase
            val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
            val result = retrofit.createAccount(account)
            return if (result.isSuccessful) {
                val responseObj = result.body()
                if (responseObj != null) {
                    if (responseObj.success) {
                        // Đăng ký thành công, gửi email xác thực
                        auth.currentUser?.sendEmailVerification()?.await()
                        "Success"
                    } else {
                        responseObj.error!!
                    }
                } else {
                    auth.signOut()
                    "Có lỗi xảy ra trong lúc Đăng ký tài khoản của bạn!"
                }
            } else {
                auth.signOut()
                result.body()?.error.toString()
            }
        } catch (e: Exception) {
            Log.d("TAG", e.toString())
            auth.signOut()
            return when (e) {
                is FirebaseNetworkException -> {
                    "Không có kết nối internet!"
                }

                is FirebaseAuthUserCollisionException -> {
                    "Email đăng ký đã tồn tại. Vui lòng đăng nhập!"
                }

                else -> {
                    "Có lỗi xảy ra. Không thể gửi email đến bạn!"
                }
            }
        }
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

    override suspend fun sendMessage(message: Message): Boolean {
        val baseUrl = "https://sendmessage-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.sendMessage(message)
        return result.isSuccessful

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

    private fun createRetrofitService(baseUrl: String) : Retrofit {
        val  gson = GsonBuilder().serializeNulls().create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}