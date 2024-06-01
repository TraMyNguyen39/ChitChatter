package com.midterm.chitchatter.data.model

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.midterm.chitchatter.R
import com.midterm.chitchatter.utils.ContactStatus
import java.io.Serializable

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey @SerializedName("email") var email: String = "",
    var password: String? = "",
    @SerializedName("displayName") var name: String = "",
    @SerializedName("gender") var gender: String? = null,
    @SerializedName("birthday") var birthday: String? = null,
    @SerializedName("imageUrl") var imageUrl: String? = null,
    @SerializedName("token") var token: String? = null,
    @SerializedName("contacts") var contacts: MutableList<String> = mutableListOf(),
    var isVerified: Boolean? = true,
    @SerializedName("contactStatus") var contactStatus: Int = ContactStatus.UNCONNECTED.ordinal
)   : Serializable{

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
    fun getCurrentAccount(context: Context): Account? {
        val accountKey = context.getString(R.string.preference_account_key)
        val emailKey = context.getString(R.string.preference_email_key)
        val tokenKey = context.getString(R.string.preference_token_key)

        val sharedPref: SharedPreferences = context.getSharedPreferences(accountKey, Context.MODE_PRIVATE)
        val email: String? = sharedPref.getString(emailKey, null)
        val token: String? = sharedPref.getString(tokenKey, null)

        return if (email != null && token != null) {
            Account(email, token)
        } else {
            null
        }
    }
}