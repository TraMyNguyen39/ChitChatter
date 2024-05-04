package com.midterm.chitchatter.data.source.remote

import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MessageService {
    @POST("/")
    suspend fun createAccount(@Body account: Account) : Response<ResponseResult>

    @POST("/")
    suspend fun updateAccount(@Body account: Account) : Response<ResponseResult>

    @POST("/")
    suspend fun getLoginAccount(@Body account: Account) : Response<Account?>

    @POST("/")
    suspend fun sendMessage(@Body message: Message) : Response<ResponseResult>
}