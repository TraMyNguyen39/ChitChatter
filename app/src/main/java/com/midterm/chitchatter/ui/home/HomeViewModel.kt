package com.midterm.chitchatter.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.ui.login.LoginViewModel
import com.midterm.chitchatter.utils.ChitChatterUtils
import kotlinx.coroutines.launch

class HomeViewModel (
    private val repository: Repository
) : ViewModel() {
    private val _currentAccount = MutableLiveData<String>()
    val currentAccount: LiveData<String>
        get() = _currentAccount

    fun setCurrentAccount(email: String) {
        _currentAccount.value = email
    }
    fun getCurrentAccount(): String? {
        return _currentAccount.value
    }

    private val _contacts = MutableLiveData<List<Account>>()
    val contacts: LiveData<List<Account>>
        get() = _contacts

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>>
        get() = _messages

    fun fetchAllLastMessages(email: String) {
        viewModelScope.launch {
            try {
                val messages = (repository as Repository.RemoteRepository).getAllLastMessages(email)
                Log.d("Size of message", messages.size.toString())
                _messages.postValue(messages)
            } catch (e: Exception) {
                // Xử lý lỗi nếu có
                Log.e("HomeViewModel", "Error fetching messages: ${e.message}")
            }
        }
    }

    fun removeToken(email: String) {
        viewModelScope.launch {
            try {
                val account = Account()
                account.email = email
                account.token = ChitChatterUtils.token
                val result = (repository as Repository.RemoteRepository).logout(account)
                Log.e("LOGOUT", "$result")
            } catch (e: Exception) {
                // Xử lý lỗi nếu có
                Log.e("LOGOUT", "Error removeToken: ${e.message}")
            }
        }
    }
}

class HomeViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not HomeViewModel")
    }
}