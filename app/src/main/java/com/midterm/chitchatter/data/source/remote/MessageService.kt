package com.midterm.chitchatter.data.source.remote

import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.AccountConnection
import com.midterm.chitchatter.data.model.Message
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


interface MessageService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAAB4y-Wag:APA91bFkXiF05lJrtHNbZhy_iOxGi2TM-JgyYA3bitXDeajc7KyU70unReN0QGYEXPUPpMhJKTpVL-iaquJV3O7_ckJ1JSh79vOcZAQxaRkDkl90HwwtK11IMUcDJITdoLbq1bLCA-KJ"
    )
    @POST("/")
    suspend fun createAccount(@Body account: Account): Response<ResponseResult<Account?>>

    @POST("/")
    suspend fun updateAccount(@Body account: Account): Response<ResponseResult<Account?>>

    @POST("/")
    suspend fun getLoginAccount(@Body account: Account) : Response<Account?>
    @POST("/")
    suspend fun logout(@Body account: Account) : Response<ResponseResult<Account?>>

    @GET("/")
    suspend fun getContactsOfAccount(
        @Query("email") email: String,
        @Query("token") token: String
    ): Response<ResponseResult<ArrayList<Account>>>
    @GET("/")
    suspend fun getContactsSearch(
        @Query("searchText") textSearch: String,
        @Query("email") email: String,
        @Query("token") token: String
    ): Response<ResponseResult<ArrayList<Account>>>

    @GET("/")
    suspend fun getContactDetail(
        @Query("email") email: String,
    ): Response<ResponseResult<Account?>>

    @GET("/")
    suspend fun getContactDetailConnection(
        @Query("email") email: String,
        @Query("contactEmail") contactEmail: String,
        @Query("token") token: String
    ): Response<ResponseResult<Account?>>

//    @Headers(
//        "Content-Type: application/json",
//        "Authorization: key=AAAAB4y-Wag:APA91bFkXiF05lJrtHNbZhy_iOxGi2TM-JgyYA3bitXDeajc7KyU70unReN0QGYEXPUPpMhJKTpVL-iaquJV3O7_ckJ1JSh79vOcZAQxaRkDkl90HwwtK11IMUcDJITdoLbq1bLCA-KJ"
//    )

    @POST("/")
    suspend fun addContact (
        @Body contactConnection: AccountConnection
    ): Response<ResponseNoResult>

    @POST("/")
    suspend fun deleteContact (
        @Body contactConnection: AccountConnection

    ): Response<ResponseNoResult>

    @POST("/")
    suspend fun acceptContact (
        @Body contactConnection: AccountConnection
    ): Response<ResponseNoResult>

    @POST("/")
    suspend fun rejectContact (
        @Body contactConnection: AccountConnection
    ): Response<ResponseNoResult>

    @POST("/")
    suspend fun sendMessage(@Body message: Message): Response<ResponseResult<Account?>>

    @GET("/")
    suspend fun getAllLastMessages(@Query("email") email: String): Response<ResponseResult<ArrayList<Message>>>

    @GET("/getChat")
    suspend fun getChat(
        @Query("sender") sender: String,
        @Query("receiver") receiver: String
    ): Response<List<Message>>

}