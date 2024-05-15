package com.midterm.chitchatter.ui.register

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

class RegisterViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _registerFormState = MutableLiveData<RegisterFormState>()
    private val _registerState = MutableLiveData<String>()

    val registerFormState: LiveData<RegisterFormState> = _registerFormState
    val registerState: LiveData<String> = _registerState // save true result or error string

    fun registerAccount(displayName: String, email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = ChitChatterUtils.token
            val account = Account(
                email, password, displayName, null, null, token = token
            )
            val result = (repository as Repository.RemoteRepository).createAccount(account)

            _registerState.postValue(result)
        }
    }

    fun registerFormChanged(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String,
    ) {
        if (email.isEmpty() || !isEmailCorrectFormat(email)) {
            _registerFormState.value = RegisterFormState(emailError = R.string.error_email)
        } else if (displayName.trim().isEmpty()) {
            _registerFormState.value =
                RegisterFormState(displayNameError = R.string.error_display_name)
        } else if (!isPasswordCorrectFormat(password)) {
            _registerFormState.value = RegisterFormState(passwordError = R.string.error_password)
        } else if (!isConfirmPasswordCorrect(password, confirmPassword)) {
            _registerFormState.value =
                RegisterFormState(confirmPasswordError = R.string.error_confirm_password)
        } else {
            _registerFormState.value = RegisterFormState(isCorrect = true)
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

    private fun isConfirmPasswordCorrect(password: String, confirmPassword: String): Boolean {
        return confirmPassword.compareTo(password) == 0
    }
}

class RegisterViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not RegisterViewModel")
    }
}