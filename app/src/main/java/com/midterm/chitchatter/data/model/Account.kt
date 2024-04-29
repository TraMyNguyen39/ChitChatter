package com.midterm.chitchatter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey @SerializedName("email") var email: String = "",
    @SerializedName("password") var password: String = "",
    @SerializedName("displayName") var name: String = "",
    @SerializedName("gender") var gender: String? = null,
    @SerializedName("imageUrl") var imageUrl: String? = null,
    @SerializedName("token") var token: String? = null,
    @SerializedName("contacts") var contacts: MutableList<String> = mutableListOf(),
    var isVerified: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        return email == other.email
    }

    override fun hashCode(): Int {
        return email.hashCode()
    }

    override fun toString(): String {
        return "Account(password='$password', email='$email', name='$name', gender=$gender, " +
                "imageUrl=$imageUrl, token=$token, contacts=$contacts)"
    }


}