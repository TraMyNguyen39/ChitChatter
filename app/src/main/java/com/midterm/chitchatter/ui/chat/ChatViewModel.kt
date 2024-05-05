package com.midterm.chitchatter.ui.chat

import android.net.Uri
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
import kotlinx.coroutines.launch

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

    fun sendMessage(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val senderAccount = LoginViewModel.currentAccount
            val notification = Notification(senderAccount.value?.name!!, text)
            val photoUrl = if (_photoUri.value == null) null else _photoUri.value.toString()
            val data = Data(text, photoUrl, _photoMimeType)
            interactingAccount.value?.let {
                val message = Message(
                    id = 0,
                    data = data,
                    notification = notification,
                    sender = senderAccount.value!!.email,
                    receiver = interactingAccount.value?.email!!,
                    token = interactingAccount.value?.token,
                    status = MessageStatus.SENT
                )
                (repository as Repository.RemoteRepository).sendMessage(message)
                addMessage(message)
                _photoMimeType = null
                _photoUri.postValue(null)
            }
        }
    }
    fun loadMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            val senderAccount = LoginViewModel.currentAccount
            val receiverAccount = interactingAccount.value
            if (receiverAccount != null && senderAccount.value != null) {
                viewModelScope.launch(Dispatchers.IO){
                    val messages = (repository as Repository.RemoteRepository).getChat(
                        senderAccount.value?.email!!,
                        receiverAccount.email
                    )
                    _listMessage.clear()
                    _listMessage.addAll(messages.reversed()) //reversed để đaảo ngược thứ tự tin nhắn từ mới nhất đến cũ nhất
                    _messages.postValue(_listMessage)
                }
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
        val status = MessageStatus.valueOf(data[KEY_STATUS] ?: MessageStatus.SENT.name)
        val messageData = Data(text, photoUrl, photoMimeType)
        val notification = Notification()
        val message = Message(
            0,
            data = messageData,
            receiver = receiver,
            sender = sender,
            token = token,
            status = status,
            notification = notification,
            timestamp = timeStamp
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

