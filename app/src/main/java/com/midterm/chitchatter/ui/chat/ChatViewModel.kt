package com.midterm.chitchatter.ui.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Data
import com.midterm.chitchatter.data.model.DataSendMessage
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.model.MessageStatus
import com.midterm.chitchatter.data.model.Notification
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.ui.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.text.SimpleDateFormat
import java.util.Locale

class ChatViewModel(
    private val repository: Repository,
    private val senderEmail: String?,
    private val receiverEmail: String?
) : ViewModel() {


    private val _interactingAccount = MutableLiveData<Account?>()
    private val _photoUri = MutableLiveData<Uri?>()
    private var _photoMimeType: String? = null
    private val _listMessage = mutableListOf<Message>()
    private val _messages = MutableLiveData<List<Message>>()

    val interactingAccount: LiveData<Account?> = _interactingAccount
    val photo: LiveData<Uri?> = _photoUri
    val messages: LiveData<List<Message>> = _messages
    val database = Firebase.database
    val sanitizedSenderEmail = senderEmail?.substringBefore('@')
    val sanitizedReceiverEmail = receiverEmail?.substringBefore('@')
    val myRef = database.getReference("messages/$sanitizedSenderEmail/$sanitizedReceiverEmail")

    init {
        Log.d("ChatViewModel", "Instance created with hashcode: ${this.hashCode()}")

        listenningForMessages()
    }
    private fun listenningForMessages() {
        myRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ChatViewModel", "reff : $myRef")
                val newMessageSnapshot = snapshot.children.lastOrNull()
                Log.d("ChatViewModel", "New message snapshot RTDB: $newMessageSnapshot")
                if (newMessageSnapshot != null) {
                    var sender: String? = null
                    var receiver: String? = null
                    var content: String? = null
                    var status: Int? = null
                    var formattedTime: String? = null
                    var photoUrl: String? = null
                    val photoMimeType: String? = null

                    for (child in snapshot.children) {
                        val key = child.key ?: continue
                        val value = child.getValue<Any>()
                        when (key) {
                            "sender" -> sender = value as? String
                            "receiver" -> receiver = value as? String
                            "content" -> content = value as? String
                            "status" -> status = value as? Int ?: MessageStatus.SENT.toInt()
                            "formattedTime" -> formattedTime = value as? String ?: SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
                            "photoUrl" -> photoUrl = value as? String
                            "photoMimeType" -> photoUrl = value as? String ?: ""
                        }
                    }

                    Log.d("ChatViewModel", "New message RTDB: $sender - $receiver - $content - $status - $formattedTime - $photoUrl - $photoMimeType")
                    val newMessage = Message(
                        "",
                        data = Data("", "", ""),
                        content = content ?: "",
                        receiver = receiver ?: "",
                        sender = sender ?: "",
                        token = "",
                        status = status ?: MessageStatus.SENT.toInt(),
                        notification = Notification(),
                        timestamp = System.currentTimeMillis(),
                        formattedTime = formattedTime ?: SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis()),
                        photoUrl = photoUrl,
                        photoMimeType = photoMimeType
                    )
                    Log.d("ChatViewModel", "New class message: $newMessage")
                    addMessage(newMessage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatViewModel", "Failed to read value.", error.toException())
            }
        })
    }



    fun updateInteractingAccount(account: Account?) {
        _interactingAccount.postValue(account)
    }
    fun updateInteractingAccountToken(token: String) {
        val currentAccount = _interactingAccount.value
        if (currentAccount != null) {
            currentAccount.token = token
            _interactingAccount.postValue(currentAccount)
            Log.d("ChatViewModel", "Token updated: $token")
        } else {
            Log.d("ChatViewModel", "interactingAccountValue is null, cannot update token")
        }
    }

    fun setPhoto(uri: Uri, mimeType: String) {
        _photoUri.value = uri
        _photoMimeType = mimeType
    }

    fun sendMessage(text: String, senderEmail: String, receiverEmail: String, token: String): Boolean {
        var isMessageSent = false
        val interactingAccountValue = interactingAccount.value
        viewModelScope.launch {
            if (senderEmail != "" && receiverEmail != ""){
                val dataSendMessage = DataSendMessage(
                    token = token,
                    sender = senderEmail,
                    receiver = receiverEmail,
                    content = text,
                    photoUrl = if (_photoUri.value == null) null else _photoUri.value.toString(),
                    photoMimeType = _photoMimeType ?: ""
                )

                val newMessage = Message(
                    "",
                    data = Data(text, _photoUri.value.toString(), _photoMimeType ?: ""),
                    content = text,
                    receiver = receiverEmail,
                    sender = senderEmail,
                    token = token,
                    status = MessageStatus.SENT.toInt(),
                    notification = Notification(),
                    timestamp = System.currentTimeMillis(),
                    formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
                )
                addMessage(newMessage)

//                isMessageSent = async { (repository as Repository.RemoteRepository).sendMessage(dataSendMessage) }.await()
                isMessageSent = withContext(Dispatchers.IO) { (repository as Repository.RemoteRepository).sendMessage(dataSendMessage) }
                // thực ra đoạn này phải check isMessageSent ...


                withContext(Dispatchers.IO) {
                    Log.d("ChatViewModel", "Sending message to server")
                    sendFCMNotification(receiverEmail, senderEmail, text)
                }


            }
        }
        return isMessageSent
    }
    fun sendMessage(text: String,photoUrl: String, photoMimeType: String, senderEmail: String, receiverEmail: String, token: String): Boolean {
        var isMessageSent = false

        val interactingAccountValue = interactingAccount.value
        viewModelScope.launch {
            if (senderEmail != "" && receiverEmail != ""){
                val dataSendMessage = DataSendMessage(
                    token = token,
                    sender = senderEmail,
                    receiver = receiverEmail,
                    content = text,
                    photoUrl = photoUrl,
                    photoMimeType = photoMimeType
                )
                Log.d("ChatViewModel", "Sending message datasend : $dataSendMessage")

                val newMessage = Message(
                    "",
                    data = Data(text, _photoUri.value.toString(), _photoMimeType ?: ""),
                    content = text,
                    receiver = receiverEmail,
                    sender = senderEmail,
                    token = token,
                    status = MessageStatus.SENT.toInt(),
                    notification = Notification(),
                    timestamp = System.currentTimeMillis(),
                    formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis())
                )
                addMessage(newMessage)

//                isMessageSent = async { (repository as Repository.RemoteRepository).sendMessage(dataSendMessage) }.await()
                isMessageSent = withContext(Dispatchers.IO) { (repository as Repository.RemoteRepository).sendMessage(dataSendMessage) }
                // thực ra đoạn này phải check isMessageSent ...


                withContext(Dispatchers.IO) {
                    Log.d("ChatViewModel", "Sending message to server")
                    sendFCMNotification(receiverEmail, senderEmail, text)
                }


            }
        }
        return isMessageSent
    }
    fun loadMessage(senderEmail: String?, receiverEmail: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!senderEmail.isNullOrBlank() && !receiverEmail.isNullOrBlank()) {
                Log.d("ChatViewModel", "load message with senderEmail: $senderEmail, receiverEmail: $receiverEmail")
                val messages = (repository as Repository.RemoteRepository).getChat(senderEmail, receiverEmail)
                Log.d("ChatViewModel", "load message: $messages")
                _listMessage.clear()
                _listMessage.addAll(messages.reversed())
                _messages.postValue(_listMessage)
            }
        }
    }

    private fun addMessage(message: Message) {
        _listMessage.add(message)
        _messages.postValue(_listMessage)

        Log.d("ChatViewModel", "New message added: $message")
    }
    fun sendFCMNotification(receiverEmail: String, senderEmail: String, message: String) {
        val payload = JSONObject()
        payload.put("to", receiverEmail)
        val data = JSONObject()
        data.put("senderEmail", senderEmail)
        data.put("message", message)
        payload.put("data", data)

        val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .post(body)
            .addHeader("Authorization", "key=AAAAB4y-Wag:APA91bFkXiF05lJrtHNbZhy_iOxGi2TM-JgyYA3bitXDeajc7KyU70unReN0QGYEXPUPpMhJKTpVL-iaquJV3O7_ckJ1JSh79vOcZAQxaRkDkl90HwwtK11IMUcDJITdoLbq1bLCA-KJ")
            .build()

        val client = OkHttpClient()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
        }
    }
    fun updateMessages() {
        viewModelScope.launch {
            val senderEmail = interactingAccount.value?.email
            val receiverEmail = interactingAccount.value?.email
            loadMessage(senderEmail, receiverEmail)
        }
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
        )
        addMessage(message)
    }
    fun uploadImageAndSendMessage(uri: Uri, receiverEmail: String, senderEmail: String, token: String) {

        val storageRef = Firebase.storage.reference
        val path = uri.lastPathSegment
        val fileName = path?.substring(path.lastIndexOf('/') )
        val imageRef = storageRef.child("images/${sanitizedSenderEmail}/${sanitizedReceiverEmail}/${fileName}")
        val photoMimeType = fileName?.substring(fileName.lastIndexOf('.')).toString()

        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                sendMessage("", downloadUri.toString(), photoMimeType, senderEmail, receiverEmail, token)
                val message = Message(
                    "",
                    data = Data("", downloadUri.toString(), photoMimeType),
                    receiver = receiverEmail,
                    sender = senderEmail,
                    token = token,
                    status = 1,
                    notification = Notification(),
                    timestamp = System.currentTimeMillis(),
                    formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis()),
                )
                addMessage(message)

            }
        }.addOnFailureListener {
            Log.e("ChatViewModel", "Failed to upload image", it)
        }
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

