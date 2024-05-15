package com.midterm.chitchatter.ui.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Data
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.model.MessageStatus
import com.midterm.chitchatter.data.model.Notification
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.ui.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ChatViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _interactingAccount = MutableLiveData<Account?>()
    private val _photoUri = MutableLiveData<Uri?>()
    private var _photoMimeType: String? = null
    private val _listMessage = mutableListOf<Message>()
    private val _messages = MutableLiveData<List<Message>>()

    val interactingAccount: LiveData<Account?> = _interactingAccount
    val photo: LiveData<Uri?> = _photoUri
    val messages: LiveData<List<Message>> = _messages

    fun updateInteractingAccount(account: Account?) {
        _interactingAccount.postValue(account)
    }

    fun setPhoto(uri: Uri, mimeType: String) {
        _photoUri.value = uri
        _photoMimeType = mimeType
    }

    fun sendMessage(text: String): Boolean {
        var isMessageSent = false
        viewModelScope.launch(Dispatchers.IO) {
            val senderAccount = LoginViewModel.currentAccount.value
            val interactingAccountValue = interactingAccount.value
            Log.d("ChatViewModel", "senderAccount: $senderAccount")
            Log.d("ChatViewModel", "interactingAccountValue: ${interactingAccountValue?.email.toString()}")
            if (senderAccount != null && interactingAccountValue != null) {
                val notification = Notification(senderAccount.name, text)
                val photoUrl = if (_photoUri.value == null) null else _photoUri.value.toString()
                val data = Data(text, photoUrl, _photoMimeType)
                val timestamp = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val status = MessageStatus.SENT.toInt()
                val message = Message(
                    id = "",
                    data = data,
                    notification = notification,
                    sender = senderAccount.email,
                    receiver = interactingAccountValue.email,
                    token = interactingAccountValue.token,
                    status = status,
                    formattedTime = dateFormat.format(timestamp),
                    currentUserEmail = senderAccount.email
                )
                isMessageSent = async { (repository as Repository.RemoteRepository).sendMessage(message) }.await()
                if (isMessageSent) {
                    addMessage(message)
                    _photoMimeType = null
                    _photoUri.postValue(null)
                }
            }
        }
        Log.d("ChatViewModel", "sendMessage: $text")
        Log.d("ChatViewModel", "isMessageSent: $isMessageSent")
        return isMessageSent
    }
    fun loadMessage(senderEmail: String?, receiverEmail: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!senderEmail.isNullOrBlank() && !receiverEmail.isNullOrBlank()) {
                val messages = (repository as Repository.RemoteRepository).getChat(senderEmail, receiverEmail)
                _listMessage.clear()
                _listMessage.addAll(messages.reversed())
                _messages.postValue(_listMessage)
            }
        }
    }

    private fun addMessage(message: Message) {
        _listMessage.add(message)
        _messages.postValue(_listMessage)
        //
    }

    fun pushIncomingMessage(data: Map<String, String>) {
        val text = data[KEY_TEXT] ?: ""
        val photoUrl = data[KEY_PHOTO_URL]
        val photoMimeType = data[KEY_PHOTO_MIME_TYPE]
        val timeStamp = data[KEY_TIMESTAMP]?.toLong() ?: 0
        val sender = data[KEY_SENDER] ?: ""
        val receiver = data[KEY_RECEIVER] ?: ""
        val token = data[KEY_TOKEN] ?: ""
        val status = MessageStatus.valueOf(data[KEY_STATUS] ?: MessageStatus.SENT.name).toInt()
        val messageData = Data(text, photoUrl, photoMimeType)
        val notification = Notification()
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val message = Message(
            "",
            data = messageData,
            receiver = receiver,
            sender = sender,
            token = token,
            status = status,
            notification = notification,
            timestamp = timeStamp,
            formattedTime = dateFormat.format(timestamp),
            currentUserEmail = LoginViewModel.currentAccount.value?.email!!
        )
        addMessage(message)
    }

    companion object {
        var isActive: Boolean = false
            get() {
                TODO()
            }
        const val KEY_TEXT = "text"
        const val KEY_PHOTO_URL = "photoUrl"
        const val KEY_PHOTO_MIME_TYPE = "photoMimeType"
        const val KEY_SENDER = "sender"
        const val KEY_RECEIVER = "receiver"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_STATUS = "status"
        const val KEY_TOKEN = "token"
    }
}

