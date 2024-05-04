package com.midterm.chitchatter.data.source

import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message

interface Repository {
    interface RemoteRepository : Repository {
        suspend fun createAccount(account: Account) : String
        suspend fun updateAccount(account: Account) : Boolean
        suspend fun login(account: Account) : Account?
        suspend fun sendResetPassword(email: String) : Int
        suspend fun sendEmailVerification(email: String) : Boolean
        suspend fun getAllLastMessages(email: String) : ArrayList<Message>
    }

    interface LocalRepository : Repository {
        // ToDo
    }
}