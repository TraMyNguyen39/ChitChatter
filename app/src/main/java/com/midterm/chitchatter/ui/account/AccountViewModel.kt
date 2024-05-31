package com.midterm.chitchatter.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.source.Repository
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _contact = MutableLiveData<Account?>()
    val contact: LiveData<Account?> = _contact

    fun loadAccountInfo(email: String?) {
        if (email != null) {
            viewModelScope.launch {
                val account = (repository as Repository.RemoteRepository).getContactDetail(email)
                _contact.postValue(account)
            }
        }
    }
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