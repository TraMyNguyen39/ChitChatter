package com.midterm.chitchatter.data.source.remote

import com.midterm.chitchatter.data.model.Account

data class Response<T>(
    var success: Boolean,
    var data: T,
    var error: String?
)