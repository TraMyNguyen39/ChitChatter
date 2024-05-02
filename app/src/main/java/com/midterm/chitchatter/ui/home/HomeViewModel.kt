package com.midterm.chitchatter.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Data
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.model.MessageStatus
import com.midterm.chitchatter.data.model.Notification
import java.util.Date

class HomeViewModel : ViewModel() {
    companion object {
        private val _currentAccount = MutableLiveData<Account>()
        val currentAccount: LiveData<Account>
            get() = _currentAccount

        private val _contacts = MutableLiveData<List<Account>>()
        val contacts: LiveData<List<Account>>
            get() = _contacts

        private val _messages = MutableLiveData<List<Message>>()
        val messages: LiveData<List<Message>>
            get() = _messages

        fun createMessage(sender: Account, receiver: Account, text: String): Message {
            val data = Data(text = text)
            val notification = Notification(title = "New Message", body = text)

            return Message(
                id = System.currentTimeMillis(),
                sender = sender.email,
                receiver = receiver.email,
                data = data,
                notification = notification,
                timestamp = Date().time,
                status = MessageStatus.SENT
            )
        }

        init {
            val account = Account(
                email = "john.doe@example.com",
                password = "123456",
                name = "John Doe",
                gender = "Male",
                imageUrl = "https://images.unsplash.com/photo-1711950901044-fa6215a9c59b?q=80&w=1770&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                contacts = mutableListOf("alice", "bob", "charlie")
            )
            _currentAccount.postValue(account)

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

            val message1 = createMessage(account, contactsList[0], "Hello Bob!")
            val message2 = createMessage(contactsList[1], account, "Hi Alice!")
            val message3 = createMessage(contactsList[2], account, "Hey John!")
            val messagesList = listOf(message1, message2, message3)
            _messages.postValue(messagesList)
        }
    }
}