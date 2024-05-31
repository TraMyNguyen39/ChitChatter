package com.midterm.chitchatter.data.source.remote

import com.midterm.chitchatter.data.model.Account
import java.lang.Exception

data class ResponseResult(
    val success: Boolean,
    val targetAccount: Account? = null,
    val error: String?,
)

//data class ResponseResult<T>(
//    val success: Boolean,
//    val data: T = null,
//    val error: String?,
//)
