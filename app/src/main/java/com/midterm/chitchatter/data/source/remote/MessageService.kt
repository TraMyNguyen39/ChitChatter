package com.midterm.chitchatter.data.source.remote
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAAB4y-Wag:APA91bFkXiF05lJrtHNbZhy_iOxGi2TM-JgyYA3bitXDeajc7KyU70unReN0QGYEXPUPpMhJKTpVL-iaquJV3O7_ckJ1JSh79vOcZAQxaRkDkl90HwwtK11IMUcDJITdoLbq1bLCA-KJ"
    )
    @POST("/createAccount")
    suspend fun createAccount(@Body account: Account): Response<ResponseResult>

    @POST("/sendMessage")
    suspend fun sendMessage(@Body message: Message): Response<ResponseResult>

    @POST("/updateAccount")
    suspend fun updateAccount(@Body account: Account): Response<ResponseResult>

    @POST("/login")
    suspend fun login(@Body account: Account): Response<Account>

    @GET("/getFriendAccounts/{username}")
    suspend fun getFriendAccounts(@Path("username") username: String): Response<List<Account>>

    @GET("/getChat/{sender}/{receiver}")
    suspend fun getChat(@Path("sender") sender: String, @Path("receiver") receiver: String): Response<List<Message>>
}