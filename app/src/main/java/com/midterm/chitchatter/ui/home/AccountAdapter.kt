package com.midterm.chitchatter.ui.home

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.model.MessageStatus
import com.midterm.chitchatter.databinding.ItemChatboxBinding

class AccountAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    private var messagesList = ArrayList<Message>();

    class ViewHolder(
        private val binding: ItemChatboxBinding,
        private val listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(lastMsg: Message?) {
//            val url = if (!lastMsg?.url.isNullOrEmpty()) {
//                lastMsg?.url
//            } else {
//
//            }
            if (!lastMsg?.url.isNullOrEmpty()) {
                val fileName = lastMsg?.url as String

                val bucketUrl = "gs://chitchatter-b97bf.appspot.com/avatars/"

                val storage: FirebaseStorage = FirebaseStorage.getInstance()
                val storageRef: StorageReference = storage.getReferenceFromUrl(bucketUrl)
                val imageRef: StorageReference = storageRef.child(fileName)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(binding.ivSender)
                        .load(uri)
                        .error(R.drawable.android)
                        .into(binding.ivSender)
                }.addOnFailureListener { exception ->
                    // Xử lý lỗi nếu có
                    Log.e("FirebaseStorage", "Failed to get download URL", exception)
                }
            } else {
                Glide.with(binding.ivSender)
                    .load("https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?q=80&w=1770&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                    .error(R.drawable.android)
                    .into(binding.ivSender)
            }


            binding.tvName.text = lastMsg?.name
            if (lastMsg != null) {
                if (lastMsg.isIncoming) {
                    binding.tvMessage.text = lastMsg.content
                    if (lastMsg.status == MessageStatus.SENT.toInt()) {
                        binding.ivReceiver.setImageResource(R.drawable.received_msg)
                        binding.tvMessage.setTypeface(null, Typeface.BOLD)
                        binding.tvName.setTypeface(null, Typeface.BOLD)
                        binding.cvReceiver.visibility = View.VISIBLE
                    }
                    else if (lastMsg.status == MessageStatus.SEEN.toInt()) {
                        binding.tvMessage.setTypeface(null, Typeface.NORMAL)
                        binding.cvReceiver.visibility = View.GONE
                    }
                } else {
                    binding.tvMessage.text = "You: ${lastMsg.content}"
                    binding.tvMessage.setTypeface(null, Typeface.NORMAL)
                    when (lastMsg.status) {
                        MessageStatus.SEEN.toInt() -> {
                            Log.d("STATUS_OF_MESSAGE", "SEEN")
//                            binding.ivReceiver.visibility = View.GONE
                            val fileName = lastMsg?.url ?: ""

                            val bucketUrl = "gs://chitchatter-b97bf.appspot.com/avatars/"

                            val storage: FirebaseStorage = FirebaseStorage.getInstance()
                            val storageRef: StorageReference =
                                storage.getReferenceFromUrl(bucketUrl)
                            val imageRef: StorageReference = storageRef.child(fileName)
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                Glide.with(binding.ivReceiver)
                                    .load(uri)
                                    .error(R.drawable.android)
                                    .into(binding.ivReceiver)
                            }
                        }

                        MessageStatus.SENDING.toInt() -> {
                            Log.d("STATUS_OF_MESSAGE", "SENDING")
                            binding.ivReceiver.setImageResource(R.drawable.sending_msg)
                        }

                        MessageStatus.SENT.toInt() -> {
                            Log.d("STATUS_OF_MESSAGE", "SENT")
                            binding.ivReceiver.setImageResource(R.drawable.sent_msg)
                        }

                        else -> {
                            // Xem như đang gửi
                            Log.d("STATUS_OF_MESSAGE", "OTHER")
                            binding.ivReceiver.setImageResource(R.drawable.sending_msg)
                        }
                    }
                }
            } else {
                binding.tvMessage.text = ""
            }

            binding.tvTime.text = lastMsg?.formattedTime


            binding.root.setOnClickListener {
                if (lastMsg != null) {
                    listener.onItemClick(lastMsg)
                }
            }

//            binding.root.setOnClickListener {
//                listener.onItemClick(lastMsg.sender)
//            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatboxBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lastMsg = messagesList[position]
        holder.bind(lastMsg)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMessage(messages: List<Message>) {
        messagesList.clear()
        messagesList.addAll(messages)
        notifyDataSetChanged()
    }

    fun updateMessage(message: Message, position: Int) {
        messagesList[position] = message
        notifyItemChanged(position)
    }

    private fun hashCode(sender: String, receiver: String): Int {
        var result = sender.hashCode()
        result = result * 31 + receiver.hashCode()
        return result
    }

//    fun updateAccounts(accounts: List<Account>) {
//        val oldSize = this.accounts.size
//        val newSize = accounts.size
//        this.accounts.clear()
//        this.accounts.addAll(accounts)
//        if (newSize > oldSize) {
//            notifyItemRangeInserted(oldSize, newSize - oldSize)
//        } else {
//            notifyItemRangeRemoved(newSize - 1, oldSize - newSize)
//        }
//    }

    interface OnItemClickListener {
        fun onItemClick(message: Message)
    }

}