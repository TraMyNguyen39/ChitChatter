package com.midterm.chitchatter.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.midterm.chitchatter.data.model.Account

class LoginViewModel {
    companion object {
        private val _currentAccount = MutableLiveData<Account?>()

        val currentAccount: LiveData<Account?> = _currentAccount

        fun updateCurrentAccount(account: Account?) {
            _currentAccount.postValue(account)
        }
    }

}