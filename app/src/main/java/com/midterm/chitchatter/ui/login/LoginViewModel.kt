package com.midterm.chitchatter.ui.login

import androidx.core.util.PatternsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.utils.ChitChatterUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel (
    private val repository: Repository
) : ViewModel() {
    private val _loginFormState = MutableLiveData<LoginFormState>()
    private val _loggedInAccount = MutableLiveData<Account?>()

    val loginFormState: LiveData<LoginFormState> = _loginFormState
    val loggedInAccount: LiveData<Account?> = _loggedInAccount
    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val account = Account(email, password, token = ChitChatterUtils.token)
            val result = (repository as Repository.RemoteRepository).login(account)
            _loggedInAccount.postValue(result)
        }
    }

    fun resetAccount() {
        _loggedInAccount.value = null
    }
    fun sendEmailVerification(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            (repository as Repository.RemoteRepository).sendEmailVerification(email)
        }
    }
    fun loginFormChange(email: String, password: String) {
        if (!isEmailCorrectFormat(email)) {
            _loginFormState.value = LoginFormState(emailError = R.string.error_email)
        } else if (!isPasswordCorrectFormat(password)) {
            _loginFormState.value = LoginFormState(passwordError = R.string.error_password)
        } else {
            _loginFormState.value = LoginFormState(isCorrect = true)
        }
    }

    private fun isEmailCorrectFormat(email: String): Boolean {
        return if (email.contains('@')) {
            PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            false
        }
    }

    private fun isPasswordCorrectFormat(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
        return regex.matches(password)
    }
}

class LoginViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not LoginViewModel")
    }
}