package com.midterm.chitchatter.ui.chat

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

private val Any.username: String?
    get() {
        TODO("Not yet implemented")
    }
val Any.displayName: String?
    get() {
        TODO("Not yet implemented")
    }
private val Any.value: Any
    get() {
        TODO("Not yet implemented")
    }

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
            val notification = Notification(senderAccount.value?.displayName!!, text)
            val photoUrl = if (_photoUri.value == null) null else _photoUri.value.toString()
            val data = Data(text, photoUrl, _photoMimeType)
            interactingAccount.value?.let {
                val message = Message(
                    id = 0,
                    data = data,
                    notification = notification,
                    sender = senderAccount.value?.username!!,
                    receiver = interactingAccount.value?.username!!,
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
            if (receiverAccount != null) {
                val messages = (repository as Repository.RemoteRepository).getChat(
                    senderAccount.value?.username!!,
                    receiverAccount.username
                )
                _listMessage.clear()
                _listMessage.addAll(messages)
                _messages.postValue(_listMessage)
            }
        }
    }

    private fun addMessage(message: Message) {
        _listMessage.add(message)
        _messages.postValue(_listMessage)
        //
    }

    fun pushIncomingMessage(data: Any) {

    }

    companion object {
        var isActive: Boolean = false
            get() {
                TODO()
            }
    }
}

