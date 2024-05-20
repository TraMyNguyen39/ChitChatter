package com.midterm.chitchatter.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.ui.login.LoginViewModel

class ContactsViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _contacts = MutableLiveData<List<Account>>()
    private val _contactDetail = MutableLiveData<Account>()

    val contacts: LiveData<List<Account>> = _contacts
    val contactDetail: LiveData<Account> = _contactDetail

    init {
        val contactsList = listOf(
            Account(
                email = "joh12n.doe@example.com",
                password = "1234512126",
                name = "Alice Smith",
                gender = "Female",
                imageUrl = "https://images.unsplash.com/photo-1711950901044-fa6215a9c59b?q=80&w=1770&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                contacts = mutableListOf("john_doe", "bob", "charlie")
            ),
            Account(
                email = "joh12n.do232e@example.com",
                password = "1234512132326",
                name = "Bob",
                gender = "Male",
                imageUrl = "https://images.unsplash.com/photo-1713746738119-b6ae17a226bb?q=80&w=1965&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                contacts = mutableListOf("john_doe", "alice", "charlie")
            ),
            Account(
                email = "o232e@example.com",
                password = "12132326",
                name = "Charlie",
                gender = "Female",
                imageUrl = "https://images.unsplash.com/photo-1712315884740-4220c556c68e?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                contacts = mutableListOf("john_doe", "alice", "bob")
            )
        )
        _contacts.postValue(contactsList)
    }

    fun selectContactDetail(contact: Account) {
        _contactDetail.value = contact
    }
}

class ContactsViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            return ContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not ContactsViewModel")
    }
}