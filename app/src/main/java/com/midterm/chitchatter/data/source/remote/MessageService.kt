package com.midterm.chitchatter.data.source.remote

import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MessageService {
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
}