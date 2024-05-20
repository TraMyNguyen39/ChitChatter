package com.midterm.chitchatter.ui.contacts

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

class ContactViewModel (
    private val repository: Repository
) : ViewModel() {
    private val _loggedInAccount = MutableLiveData<Account?>()
    val loggedInAccount: LiveData<Account?> = _loggedInAccount
}

class ContactViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            return ContactViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not ContactViewModel")
    }
}