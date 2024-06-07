package com.midterm.chitchatter.data.source.remote

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.AccountConnection
import com.midterm.chitchatter.data.model.ContactRequestSender
import com.midterm.chitchatter.data.model.DataSendMessage
import com.midterm.chitchatter.data.model.DataUpdateStatus
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.source.DataSource
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

    override suspend fun updateAvatar(account: Account): Boolean {
        val baseUrl = "https://updateavatar-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.updateAvatar(account)
        return result.isSuccessful
    }

    override suspend fun login(account: Account): Account? {
        val baseUrl = "https://getcurrentaccount-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)

        val auth: FirebaseAuth = Firebase.auth
        try {
            val accountAuth =
                auth.signInWithEmailAndPassword(account.email, account.password!!).await()
            if (accountAuth.user != null) {
                if (accountAuth.user!!.isEmailVerified) {
                    account.password = null // Avoid leak password
                    val result = retrofit.getLoginAccount(account)
                    if (result.isSuccessful) {
                        return result.body()
                    }
                } else {
                    return Account(isVerified = false)
                }
            }
        } catch (e: Exception) {
            Log.e("LOGIN", e.message!!)
        }
        auth.signOut()
        return null
    }

    override suspend fun logout(account: Account): Boolean {
        val baseUrl = "https://logout-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.logout(account)
        return result.isSuccessful
    }

    override suspend fun sendResetPassword(email: String): Int {
        val auth: FirebaseAuth = Firebase.auth
        return try {
            auth.sendPasswordResetEmail(email).await()
            R.string.msg_check_email_reset_password
        } catch (e: Exception) {
            R.string.msg_cant_send_email
        }
    }

    override suspend fun sendEmailVerification(email: String): Boolean {
        val auth: FirebaseAuth = Firebase.auth
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            auth.signOut()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getContactDetail(email: String): Account? {
        val baseUrl = "https://getaccountbyemail-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)

        try {
            val response = retrofit.getContactDetail(email)

            if (response.isSuccessful) {
                return response.body()?.data
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }
        return null
    }

    override suspend fun getContactDetailConnection(userEmail: String, contactEmail: String, token: String): Account? {
        val baseUrl = "https://getcontactaccount-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)

        try {
            val response = retrofit.getContactDetailConnection(userEmail, contactEmail, token)

            if (response.isSuccessful) {
                return response.body()?.data
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }
        return null
    }

    override suspend fun addContact(
        userEmail: String,
        contactEmail: String,
        token: String
    ): Boolean {
        val baseUrl = "https://addcontact-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)

        try {
            val response = retrofit.addContact(AccountConnection(userEmail, contactEmail, token))
            if (response.isSuccessful) {
                return true
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
                print(response)
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }
        return false
    }

    override suspend fun deleteContact(
        userEmail: String,
        contactEmail: String,
        token: String
    ): Boolean {
        val baseUrl = "https://deletecontact-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)

        try {
            val response = retrofit.deleteContact(AccountConnection(userEmail, contactEmail, token))
            if (response.isSuccessful) {
                return true
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }
        return false
    }

    override suspend fun acceptContact(
        userEmail: String,
        contactEmail: String,
        token: String
    ): Boolean {
        val baseUrl = "https://acceptcontact-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)

        try {
            val response = retrofit.acceptContact(AccountConnection(userEmail, contactEmail, token))
            if (response.isSuccessful) {
                return true
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }
        return false
    }

    override suspend fun rejectContact(
        userEmail: String,
        contactEmail: String,
        token: String
    ): Boolean {
        val baseUrl = "https://rejectcontact-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)

        try {
            val response = retrofit.rejectContact(AccountConnection(userEmail, contactEmail, token))
            if (response.isSuccessful) {
                return true
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }
        return false
    }

    override suspend fun getContactsOfAccount(email: String, token: String): ArrayList<Account> {
        val baseUrl = "https://getcontactsofaccount-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        try {
            val response = retrofit.getContactsOfAccount(email, token)

            if (response.isSuccessful) {
                return response.body()?.data ?: ArrayList()
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }

        return ArrayList()
    }

    override suspend fun getContactRequests(
        email: String,
        token: String
    ): ArrayList<ContactRequestSender> {
        val baseUrl = "https://getcontactrequests-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        try {
            val response = retrofit.getContactRequests(email, token)

            if (response.isSuccessful) {
                return response.body()?.data ?: ArrayList()
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }

        return ArrayList()
    }

    override suspend fun countUnreadNotifications(email: String, token: String): Int {
        val baseUrl = "https://countunreadnotifications-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        try {
            val response = retrofit.countUnreadNotifications(email, token)

            if (response.isSuccessful) {
                return response.body()?.data ?: 0
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }
        return 0
    }

    override suspend fun markAllAsRead(email: String, token: String): Boolean {
        val baseUrl = "https://markallasread-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        try {
            val response = retrofit.markAllAsRead(email, token)

            return if (response.isSuccessful) {
                true
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
                false
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }
        return false
    }

    override suspend fun getContactsSearch(
        textSearch: String,
        email: String,
        token: String
    ): ArrayList<Account> {
        val baseUrl = "https://searchcontacts-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        try {
            val response = retrofit.getContactsSearch(textSearch, email, token)

            if (response.isSuccessful) {
                return response.body()?.data ?: ArrayList()
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }

        return ArrayList()
    }

    override suspend fun getAllLastMessages(email: String): ArrayList<Message> {
        val baseUrl = "https://getalllastmessages-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        try {
            val response = retrofit.getAllLastMessages(email)

            if (response.isSuccessful) {
                return response.body()?.data ?: ArrayList()
            } else {
                Log.e(
                    "API Request",
                    "Request failed with code: ${response.code()}, ${response.body()?.error}"
                )

            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }

        return ArrayList()
    }

    override suspend fun sendMessage(message: Message): Boolean {
        val baseUrl = "https://sendmessage-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        val result = retrofit.sendMessage(message)
        if (result.isSuccessful) {
            return result.body()?.success ?: false
        }
        return false
    }
    override suspend fun sendMessage(message: DataSendMessage): Boolean {
        val baseUrl = "https://sendmessage-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        try {
            val result = retrofit.sendMessage(message)

            if (result.isSuccessful) {
                Log.d("API Request", "Request successful wwith susccess: ${result.body()?.success}")
                return result.body()?.success ?: false
            }
            else{
                Log.e("API Request", "Request failed with code: ${result.code()}, ${result.body()?.error}")
            }
        } catch (e: Exception) {
            Log.e("API Request", "Error occurred: ${e.message}")
        }
//        val result = retrofit.sendMessage(message)
//        if (result.isSuccessful) {
//            return result.body()?.success ?: false
//        }
        return false
    }

    override suspend fun getChat(sender: String, receiver: String): List<Message> {
        val baseUrl = "https://getchat-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        Log.d("API", "Calling getChat API with sender: $sender, receiver: $receiver")
        try {
            val result = retrofit.getChat(sender, receiver)
            if (result.isSuccessful) {
                Log.d(
                    "API",
                    "getChat API call successful, received ${result.body()?.size ?: 0} messages"
                )
                return result.body() ?: emptyList()
            } else {
                Log.d(
                    "API",
                    "getChat API call failed with response code: ${result.code()}, response body: ${
                        result.errorBody()?.string()
                    }"
                )
            }
        } catch (e: Exception) {
            Log.d("API", "getChat API call failed with exception: ${e.message}")
        }
        Log.d("API", "Ending getChat API call with sender: $sender, receiver: $receiver")
        return emptyList()
    }

    override suspend fun updateMessageStatus(
        data: DataUpdateStatus
    ): Boolean {
        val baseUrl = "https://updatemessagestatus-kz4isf6rva-uc.a.run.app"
        val retrofit = createRetrofitService(baseUrl).create(MessageService::class.java)
        try {
            val result = retrofit.updateMessageStatus(data)
            if (result.isSuccessful) {
                Log.d(
                    "API update",
                    "update status successfully -  ${result.body().toString()} "
                )
                return true
            } else {
                Log.d(
                    "API update",
                    "update status failed -  ${result.body().toString()} "
                )
            }
        } catch (e: Exception) {
            Log.d("API update", "exception: ${e.message}")
        }
        Log.d("API", "Ending API update status")
        return false
    }

    private fun createRetrofitService(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}