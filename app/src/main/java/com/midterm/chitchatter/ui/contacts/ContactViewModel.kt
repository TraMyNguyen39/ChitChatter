package com.midterm.chitchatter.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.utils.ChitChatterUtils
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
    fun loadAllContact (email: String?) {
        if (email != null) {
            viewModelScope.launch {
                val contactsList = (repository as Repository.RemoteRepository).getContactsOfAccount(
                    email, ChitChatterUtils.token!!
                )
                contactsList.sortedBy { it.name }
                _contacts.postValue(contactsList)
            }
        }
    }

    private var searchJob: Job? = null

    fun searchDebounced(searchText: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // wait for 500ms of inactivity
            if (_contacts.value != null) {
                val results = _contacts.value!!.filter { contact ->
                    contact.name.contains(searchText, ignoreCase = true)
                }
                // do something with results, e.g. update a LiveData object
                _searchResults.value = results
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