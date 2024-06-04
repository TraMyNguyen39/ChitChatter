package com.midterm.chitchatter.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.utils.ChitChatterUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _contact = MutableLiveData<Account?>()
    val contact: LiveData<Account?> = _contact

    fun loadAccountInfo(email: String?, contactEmail: String? = null) {
        if (email != null) {
            viewModelScope.launch {
                if (contactEmail != null) {
                    val account =
                        (repository as Repository.RemoteRepository).getContactDetailConnection(
                            email,
                            contactEmail,
                            ChitChatterUtils.token!!
                        )
                    _contact.postValue(account)
                } else {
                    val account =
                        (repository as Repository.RemoteRepository).getContactDetail(email)
                    _contact.postValue(account)
                }
            }
        }
    }

    fun addContact(userEmail: String, contactEmail: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val token = ChitChatterUtils.token!!
            val isSuccessful = (repository as Repository.RemoteRepository).addContact(
                userEmail,
                contactEmail,
                token
            )
            callback(isSuccessful)
        }
    }

    fun deleteContact(userEmail: String, contactEmail: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val token = ChitChatterUtils.token!!
            val isSuccessful = (repository as Repository.RemoteRepository).deleteContact(
                userEmail,
                contactEmail,
                token
            )
            callback(isSuccessful)
        }
    }

    fun acceptContact(userEmail: String, contactEmail: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val token = ChitChatterUtils.token!!
            val isSuccessful = (repository as Repository.RemoteRepository).acceptContact(
                userEmail,
                contactEmail,
                token
            )
            callback(isSuccessful)
        }
    }

    fun rejectContact(userEmail: String, contactEmail: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val token = ChitChatterUtils.token!!
            val isSuccessful = (repository as Repository.RemoteRepository).rejectContact(
                userEmail,
                contactEmail,
                token
            )
            callback(isSuccessful)
        }
    }

    private var searchJob: Job? = null
}

class AccountViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not AccountViewModel")
    }
}