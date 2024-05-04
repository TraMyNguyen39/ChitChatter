package com.midterm.chitchatter.data.source

import com.midterm.chitchatter.data.model.Account

interface DataSource {
    interface RemoteDataSource {
        suspend fun createAccount(account: Account) : String
        suspend fun updateAccount(account: Account) : Boolean
        suspend fun login(account: Account) : Account?
        suspend fun sendResetPassword(email: String) : Int
        suspend fun sendEmailVerification(email: String) : Boolean
    }

    interface LocalDataSource {
        // Todo
    }
}