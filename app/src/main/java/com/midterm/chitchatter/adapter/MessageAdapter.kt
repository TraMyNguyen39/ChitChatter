package com.midterm.chitchatter.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.databinding.ItemContainerSentMessageBinding
import com.midterm.chitchatter.databinding.ItemRecieveMessageBinding

class MessageAdapter(
    private val avtUrl: String?,
    private val longListener: OnItemLongClickListener,
    private val listener: OnItemClickListener,
    private val list: MutableList<Message> = ArrayList(),
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var currentAccountEmail: String? = null

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    fun setCurrentAccountEmail(email: String) {
        currentAccountEmail = email
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].sender == currentAccountEmail) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemContainerSentMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            SentMessageViewHolder(binding, longListener, listener)
        } else {
            val binding = ItemRecieveMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ReceivedMessageViewHolder(binding, avtUrl, longListener, listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = list[position]
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).bind(message)
        } else {
            (holder as ReceivedMessageViewHolder).bind(message)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
//        this.currentList.clear()
        list.clear()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<Message>) {
        val tmpList = list.toList()
        this.list.clear()
        this.list.addAll(tmpList)
        notifyDataSetChanged()
    }
    class SentMessageViewHolder(
        private val binding: ItemContainerSentMessageBinding,
        private val longListener: OnItemLongClickListener,
        private val listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            Log.d("MessageAdapter", "message: ${message}")
            binding.tvMessage.text = message.content
            binding.tvTime.text = message.formattedTime

            if (message.photoUrl != null) {
                Glide.with(binding.imageView)
                    .load(message.photoUrl)
                    .into(binding.imageView)
                binding.imageView.visibility = View.VISIBLE
                Log.d("MessageAdapter", "photoUrl: ${message.photoUrl}")

            } else {
                binding.imageView.visibility = View.GONE
            }

            if (message.content != null && message.content.isNotBlank()) {
                binding.tvMessage.visibility = View.VISIBLE
            } else {
                binding.tvMessage.visibility = View.GONE
            }

            binding.tvMessage.setOnClickListener {
                binding.tvTime.visibility =
                    if (binding.tvTime.visibility == View.GONE) View.VISIBLE
                    else View.GONE

            }
            binding.tvMessage.setOnLongClickListener {
                longListener.onDeleteMessage(message)
                true
            }
            binding.imageView.setOnClickListener {
                if (message.photoUrl != null) {
                    listener.onZoomImage(message.photoUrl)
                }
            }
        }
    }

    class ReceivedMessageViewHolder(
        private val binding: ItemRecieveMessageBinding,
        private val avtUrl: String?,
        private val longListener: OnItemLongClickListener,
        private val listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.content
            binding.tvTime.text = message.formattedTime

            if (avtUrl != null) {
                try {
                    val bucketUrl = "gs://chitchatter-b97bf.appspot.com/avatars/"

                    val storage: FirebaseStorage = FirebaseStorage.getInstance()
                    val storageRef: StorageReference = storage.getReferenceFromUrl(bucketUrl)
                    val imageRef: StorageReference = storageRef.child(avtUrl)
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(binding.imageProfile)
                            .load(uri)
                            .error(R.drawable.chitchatter)
                            .into(binding.imageProfile)
                    }.addOnFailureListener { exception ->
                        // Xử lý lỗi nếu có
                        Log.e("FirebaseStorage", "Failed to get download URL", exception)
                    }
                } catch (e: Exception) {
                    Glide.with(binding.imageView)
                        .load(R.drawable.chitchatter)
                        .error(R.drawable.chitchatter)
                        .into(binding.imageView)
                }
            } else {
                Glide.with(binding.imageView)
                    .load(R.drawable.chitchatter)
                    .error(R.drawable.chitchatter)
                    .into(binding.imageView)
            }

            if (message.photoUrl != null) {
                Glide.with(binding.imageView)
                    .load(message.photoUrl)
                    .error(R.drawable.ic_loading)
                    .into(binding.imageView)
                binding.imageView.visibility = View.VISIBLE
            } else {
                binding.imageView.visibility = View.GONE
            }

            if (message.content.isNotBlank()) {
                binding.linearText.visibility = View.VISIBLE
            } else {
                binding.linearText.visibility = View.GONE
            }

            binding.tvMessage.setOnClickListener {
                binding.tvTime.visibility =
                    if (binding.tvTime.visibility == View.GONE) View.VISIBLE
                    else View.GONE

            }

            binding.tvMessage.setOnLongClickListener {
                longListener.onDeleteMessage(message)
                true
            }

            binding.imageView.setOnClickListener {
                if (message.photoUrl != null) {
                    listener.onZoomImage(message.photoUrl)
                }
            }
        }
    }

    interface OnItemLongClickListener {
        fun onDeleteMessage(account: Message)
    }

    interface OnItemClickListener {
        fun onZoomImage(imageUrl: String)
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        // Replace this with your own logic
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        // Replace this with your own logic
        return oldItem == newItem
    }
}