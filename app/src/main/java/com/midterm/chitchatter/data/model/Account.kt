package com.midterm.chitchatter.data.model

data class Account(
    var username: String = "",
    var password: String = "",
    var email: String = "",
    var name: String = "",
    var gender: String? = null,
    var token: String? = null,
    var imageUrl: String? = null,
    var contacts: MutableList<String> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account) return false
        return username == other.username
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }

    override fun toString(): String {
        return "Account(username='$username', password='$password', " +
                "email='$email', name='$name', gender=$gender, " +
                "token=$token, imageUrl=$imageUrl, contacts=$contacts)"
    }
}