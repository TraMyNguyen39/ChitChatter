package com.midterm.chitchatter.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Data
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.model.Notification
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.ui.login.LoginViewModel
import com.midterm.chitchatter.utils.ChitChatterUtils
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _currentAccount = MutableLiveData<String>()
    val currentAccount: LiveData<String>
        get() = _currentAccount

    fun setCurrentAccount(email: String) {
        _currentAccount.value = email
        initializePaths(email)
    }

    fun getCurrentAccount(): String? {
        return _currentAccount.value
    }

    private val _contacts = MutableLiveData<List<Account>>()
    val contacts: LiveData<List<Account>>
        get() = _contacts

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>>
        get() = _messages


    private lateinit var receiver: String
    private lateinit var messagePath: String
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var messagesRef: DatabaseReference

    fun fetchAllLastMessages(email: String) {
        viewModelScope.launch {
            try {
                val messages = (repository as Repository.RemoteRepository).getAllLastMessages(email)
                val currentUserEmail = getCurrentAccount() ?: ""
                val updatedMessages = ArrayList<Message>()
                messages.forEach { message ->
                    updatedMessages.add(message)
                }
                _messages.postValue(updatedMessages)
            } catch (e: Exception) {
                // Xử lý lỗi nếu có
                Log.e("HomeViewModel", "Error fetching messages: ${e.message}")
            }
        }
    }

    fun removeToken(email: String) {
        viewModelScope.launch {
            try {
                val account = Account()
                account.email = email
                account.token = ChitChatterUtils.token
                val result = (repository as Repository.RemoteRepository).logout(account)
                Log.e("LOGOUT", "$result")
            } catch (e: Exception) {
                // Xử lý lỗi nếu có
                Log.e("LOGOUT", "Error removeToken: ${e.message}")
            }
        }
    }

    private fun initializePaths(email: String) {
        receiver = email.substringBefore('@')
        messagePath = "messages/$receiver"
        messagesRef = firebaseDatabase.getReference(messagePath)
        startListeningForMessages()
    }

    private fun startListeningForMessages() {
        messagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                val newMessage: Message = ChitChatterUtils.convertSnapshotToMessage(snapshot, _currentAccount.value ?: "")
//                _messages.postValue(_messages.value?.plus(newMessage) ?: listOf(newMessage))
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updatedMessage: Message =
                    ChitChatterUtils.convertSnapshotToMessage(snapshot, _currentAccount.value ?: "")
                Log.d("NEW_MESSAGE", updatedMessage.toString())
                val currentMessages = _messages.value ?: emptyList()
                val updatedMessages = mutableListOf<Message>()

                var newestMessage : Message = updatedMessage
                if (currentMessages.isEmpty()) {
//                    updatedMessages.add(updatedMessage)
                    newestMessage = updatedMessage
                }
                else {
                    for (message in currentMessages) {
                        if (
                            (message.sender == updatedMessage.sender && message.receiver == updatedMessage.receiver) ||
                            (message.sender == updatedMessage.receiver && message.receiver == updatedMessage.sender)
                        ) {
                            newestMessage =
                                Message(
                                    id = updatedMessage.id,
                                    sender = updatedMessage.sender,
                                    receiver = updatedMessage.receiver,
                                    data = Data(
                                        text = updatedMessage.data.text,
                                        photoUrl = updatedMessage.data.photoUrl,
                                        photoMimeType = updatedMessage.data.photoMimeType
                                    ),
                                    notification = Notification(
                                        title = updatedMessage.notification.title,
                                        body = updatedMessage.notification.body
                                    ),
                                    timestamp = updatedMessage.timestamp,
                                    status = updatedMessage.status,
                                    token = updatedMessage.token,
                                    name = message.name,
                                    content = updatedMessage.content,
                                    url = message.url,
                                    formattedTime = updatedMessage.formattedTime)

                        } else {
                            updatedMessages.add(message)
                        }
                    }
                }
                updatedMessages.add(newestMessage)
                _messages.postValue(updatedMessages.reversed())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedMessage: Message =
                    ChitChatterUtils.convertSnapshotToMessage(snapshot, _currentAccount.value ?: "")
                _messages.postValue(_messages.value?.filterNot { it.id == removedMessage.id })
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle if needed
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

}

class HomeViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Argument is not HomeViewModel")
    }
}