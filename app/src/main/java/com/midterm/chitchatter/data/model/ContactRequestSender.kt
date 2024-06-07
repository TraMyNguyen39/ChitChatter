package com.midterm.chitchatter.data.model

import java.util.Date

data class ContactRequestSender(
    var email: String = "",
    var displayName: String = "",
    var imageUrl: String? = null,
    var time: String? = null
)
