package com.midterm.chitchatter.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.utils.ChitChatterUtils
import com.midterm.chitchatter.utils.ContactStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContactViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _contacts = MutableLiveData<List<Account>>()
    val contacts: LiveData<List<Account>> = _contacts

    private val _searchResults = MutableLiveData<List<Account>>()
    val searchResults: LiveData<List<Account>> = _searchResults
    fun loadAllContact(email: String?) {
        if (email != null) {
            viewModelScope.launch {
                val contactsList = (repository as Repository.RemoteRepository).getContactsOfAccount(
                    email, ChitChatterUtils.token!!
                )

                for (contact in contactsList) {
                    contact.contactStatus = ContactStatus.CONNECTED.ordinal
                }
                contactsList.sortedBy { it.name }
                _contacts.postValue(contactsList)
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

    fun removeContact(userEmail: String, contactEmail: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val token = ChitChatterUtils.token!!
            val isSuccessful = (repository as Repository.RemoteRepository).removeContact(
                userEmail,
                contactEmail,
                token
            )
            callback(isSuccessful)
        }
    }

    private var searchJob: Job? = null

    fun searchDebounced(searchText: String, email: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // wait for 500ms of inactivity
            if (searchText.isBlank()) {
                _searchResults.postValue(_contacts.value)
            } else {
                val listAccount = (repository as Repository.RemoteRepository)
                    .getContactsSearch(searchText, email, ChitChatterUtils.token!!)
                _searchResults.postValue(listAccount)
            }
        }
    }

}

class ContactViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            return ContactViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not ContactsViewModel")
    }
}