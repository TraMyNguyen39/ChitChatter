package com.midterm.chitchatter.ui.contact_request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.midterm.chitchatter.data.model.ContactRequestSender
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.utils.ChitChatterUtils
import kotlinx.coroutines.launch

class ContactRequestViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _contactRequests = MutableLiveData<List<ContactRequestSender>>()
    val contactRequests: LiveData<List<ContactRequestSender>> = _contactRequests

    fun loadAllRequests(email: String?) {
        if (email != null) {
            viewModelScope.launch {
                val contactRequests =
                    (repository as Repository.RemoteRepository).getContactRequests(
                        email, ChitChatterUtils.token!!
                    )
                _contactRequests.postValue(contactRequests)
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

    fun deleteRequest(contactRequestSender: ContactRequestSender) {
        val list = _contactRequests.value?.toMutableList()
        list?.let {
            it.remove(contactRequestSender)
            _contactRequests.postValue(it)
        }
    }

    fun markAllAsRead(email: String) {
        viewModelScope.launch {
            (repository as Repository.RemoteRepository).markAllAsRead(
                email,
                ChitChatterUtils.token!!
            )
            repository.removeContactRequestFromRealtimeDB(
                email,
                ChitChatterUtils.token!!
            )
        }
    }
}

class ContactRequestViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactRequestViewModel::class.java)) {
            return ContactRequestViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not ContactRequestViewModel")
    }
}