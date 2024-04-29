package com.midterm.chitchatter.ui.forget_password

import androidx.core.util.PatternsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.source.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ForgetPasswordViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _forgetPassFormState = MutableLiveData<Int?>()
    val forgetPassFormState: LiveData<Int?> = _forgetPassFormState

    private val _forgetPassState = MutableLiveData<Int>()
    val forgetPassState: LiveData<Int> = _forgetPassState

    fun resetPassword(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = (repository as Repository.RemoteRepository).sendResetPassword(email)
            _forgetPassState.postValue(result)
        }
    }

    fun forgetPasswordFormChanged(
        email: String
    ) {
        if (email.isEmpty() || !isEmailCorrectFormat(email)) {
            _forgetPassFormState.value = R.string.error_email
        }  else {
            _forgetPassFormState.value = null
        }
    }

    private fun isEmailCorrectFormat(email: String): Boolean {
        return if (email.contains('@')) {
            PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            false
        }
    }
}

class ForgetPasswordViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgetPasswordViewModel::class.java)) {
            return ForgetPasswordViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not ForgetPasswordViewModel")
    }
}