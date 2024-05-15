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
import retrofit2.http.Query

interface MessageService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAAB4y-Wag:APA91bFkXiF05lJrtHNbZhy_iOxGi2TM-JgyYA3bitXDeajc7KyU70unReN0QGYEXPUPpMhJKTpVL-iaquJV3O7_ckJ1JSh79vOcZAQxaRkDkl90HwwtK11IMUcDJITdoLbq1bLCA-KJ"
    )
    @POST("/")
    suspend fun createAccount(@Body account: Account) : Response<ResponseResult>

    @POST("/")
    suspend fun updateAccount(@Body account: Account) : Response<ResponseResult>

    @POST("/")
    suspend fun getLoginAccount(@Body account: Account) : Response<Account?>

    @POST("/")
    suspend fun sendMessage(@Body message: Message) : Response<ResponseResult>

    @GET("/")
    suspend fun getAllLastMessages(@Query("email") email: String): Response<com.midterm.chitchatter.data.source.remote.Response<ArrayList<Message>>>

    @GET("/getChat")
    suspend fun getChat(@Query("sender") sender: String, @Query("receiver") receiver: String): Response<List<Message>>
}