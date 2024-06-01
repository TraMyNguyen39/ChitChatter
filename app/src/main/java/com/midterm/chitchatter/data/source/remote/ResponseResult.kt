package com.midterm.chitchatter.data.source.remote
data class ResponseResult<T>(
    var success: Boolean,
    var data: T,
    var error: String?
)