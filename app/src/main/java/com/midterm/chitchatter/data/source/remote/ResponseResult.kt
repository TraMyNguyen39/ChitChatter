package com.midterm.chitchatter.data.source.remote

import com.midterm.chitchatter.data.model.Account

data class ResponseResult(
    val success: Boolean,
    val targetAccount: Account? = null,
    val error: String?,
)