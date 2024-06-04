package com.midterm.chitchatter.data.source.remote
data class ResponseResult<T>(
    var success: Boolean,
    var data: T? = null,
    var error: String?
)

data class ResponseNoResult(
    var success: Boolean,
    var error: String?
)