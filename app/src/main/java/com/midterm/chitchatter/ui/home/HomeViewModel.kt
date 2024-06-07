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
import com.google.firebase.firestore.FirebaseFirestore
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Data
import com.midterm.chitchatter.data.model.DataUpdateStatus
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.model.Notification
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.utils.ChitChatterUtils
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: Repository
) : ViewModel() {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

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
    private lateinit var statusMessagePath: String
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var messagesRef: DatabaseReference
    private lateinit var statusMessageRef: DatabaseReference
    private var isUpdateStatusFirstTime: Boolean = true

    fun fetchAllLastMessages(email: String) {
        viewModelScope.launch {
            try {
                val messages = (repository as Repository.RemoteRepository).getAllLastMessages(email)
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

//    fun countUnreadNotifications(email: String, callback: (Int) -> Unit) {
//        viewModelScope.launch {
//            val count = (repository as Repository.RemoteRepository).countUnreadNotifications(
//                email,
//                ChitChatterUtils.token!!
//            )
//            callback(count)
//        }
//    }

    private fun initializePaths(email: String) {
        receiver = email.substringBefore('@')
        messagePath = "messages/$receiver"
        statusMessagePath = "statusMessage/${getCurrentAccount()?.substringBefore('@')}"
        Log.d("STATUS_MESSAGE_PATH", statusMessagePath)
        messagesRef = firebaseDatabase.getReference(messagePath)
        statusMessageRef = firebaseDatabase.getReference(statusMessagePath)
        startListeningForMessages()
        startListeningForStatusMessages()
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
                                    url = updatedMessage.url,
                                    formattedTime = updatedMessage.formattedTime)

                        } else {
                            updatedMessages.add(message)
                        }
                    }
                }
                updatedMessages.add(0, newestMessage)
                _messages.postValue(updatedMessages)
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

    private fun startListeningForStatusMessages() {
        statusMessageRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (isUpdateStatusFirstTime) {
                    isUpdateStatusFirstTime = false
                }
                else {
                    val statusUpdated = ChitChatterUtils.convertSnapshotToStatusMessage(snapshot)
                    val currentMessages = _messages.value ?: emptyList()
                    val updatedMessages = mutableListOf<Message>()
                    for (message in currentMessages) {
                        if (message.id == statusUpdated.id) {
                            val newMessage =
                                Message(
                                    id = message.id,
                                    sender = message.sender,
                                    receiver = message.receiver,
                                    data = Data(
                                        text = if (message.data == null) "" else message.data.text,
                                        photoUrl = if (message.data == null) "" else message.data.photoUrl,
                                        photoMimeType = if (message.data == null) "" else message.data.photoMimeType
                                    ),
                                    notification = Notification(
                                        title = if (message.notification == null) "" else message.notification.title,
                                        body = if (message.notification == null) "" else message.notification.body
                                    ),
                                    timestamp = message.timestamp,
                                    status = statusUpdated.status,
                                    token = message.token,
                                    name = message.name,
                                    content = message.content,
                                    url = message.url,
                                    formattedTime = message.formattedTime)
                            updatedMessages.add(newMessage)
                        }
                        else {
                            updatedMessages.add(message)
                        }
                    }
                    _messages.postValue(updatedMessages)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
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

    fun updateOnlineStatus(isOnline: Boolean, email: String, token: String) {
        if (email.isNotBlank()) {
            val accountsRef = firestore.collection("accounts").document(email)

            accountsRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val tokens = documentSnapshot.get("tokens") as? List<Map<String, Any>>
                    tokens?.let { tokenList ->
                        val updatedTokens = tokenList.map { tokenMap ->
                            if (tokenMap["token"] == token) {
                                tokenMap.toMutableMap().apply { put("isOnline", isOnline) }
                            } else {
                                tokenMap
                            }
                        }
                        accountsRef.update("tokens", updatedTokens).addOnSuccessListener {
                            Log.d("OnlineStatus", "Token status updated for email: $email")
                        }.addOnFailureListener { e ->
                            Log.w("OnlineStatus", "Error updating token status for email: $email", e)
                        }
                    }
                } else {
                    Log.d("OnlineStatus", "Email not found: $email")
                }
            }.addOnFailureListener { e ->
                Log.w("OnlineStatus", "Error finding email: $email", e)
            }
        } else {
            Log.w("OnlineStatus", "Invalid email: $email")
        }
    }

    fun updateMessageStatus(data: DataUpdateStatus) {
        viewModelScope.launch {
            try {
                val result = (repository as Repository.RemoteRepository).updateMessageStatus(data)
                if (result) {
                    val currentMessages = _messages.value ?: emptyList()
                    val updatedMessages = mutableListOf<Message>()
                    for (message in currentMessages) {
                        if (message.id == data.id) {
                            val newMessage =
                                Message(
                                    id = message.id,
                                    sender = message.sender,
                                    receiver = message.receiver,
                                    data = Data(
                                        text = message.data.text,
                                        photoUrl = message.data.photoUrl,
                                        photoMimeType = message.data.photoMimeType
                                    ),
                                    notification = Notification(
                                        title = message.notification.title,
                                        body = message.notification.body
                                    ),
                                    timestamp = message.timestamp,
                                    status = data.status,
                                    token = message.token,
                                    name = message.name,
                                    content = message.content,
                                    url = message.url,
                                    formattedTime = message.formattedTime)
                            updatedMessages.add(newMessage)
                        }
                        else {
                            updatedMessages.add(message)
                        }
                    }
                    _messages.postValue(updatedMessages)
                }
            } catch (e: Exception) {
                // Xử lý lỗi nếu có
                Log.e("HomeViewModel", "Error fetching messages: ${e.message}")
            }
        }
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