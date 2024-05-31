package com.midterm.chitchatter.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.midterm.chitchatter.data.source.Repository

class ChatViewModelFactory(
    private val repository: Repository,
    private val senderEmail: String?,
    private val receiverEmail: String?
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(repository, senderEmail, receiverEmail) as T
        }
        throw IllegalArgumentException("Argument must be class ChatViewModel")
    }
}
