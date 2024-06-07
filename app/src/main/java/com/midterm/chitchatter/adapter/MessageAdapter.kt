package com.midterm.chitchatter.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.databinding.ItemRecieveMessageBinding
import com.midterm.chitchatter.databinding.ItemContainerSentMessageBinding

class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private var currentAccountEmail: String? = null

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    fun setCurrentAccountEmail(email: String) {
        currentAccountEmail = email
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).sender == currentAccountEmail) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemRecieveMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).bind(message)
        } else {
            (holder as ReceivedMessageViewHolder).bind(message)
        }
    }
    fun clear() {
        this.currentList.clear()
        notifyDataSetChanged()
    }
    class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            Log.d("MessageAdapter", "message: ${message}")
            binding.tvMessage.text = message.content
            binding.tvTime.text = message.formattedTime

            if (message?.photoUrl != null) {
                Glide.with(binding.imageView)
                    .load(message.photoUrl)
                    .into(binding.imageView)
                binding.imageView.visibility = android.view.View.VISIBLE
                binding.tvMessage.visibility = android.view.View.GONE
                Log.d("MessageAdapter", "photoUrl: ${message.photoUrl}")

            } else {
                binding.imageView.visibility = android.view.View.GONE
                binding.tvMessage.visibility = android.view.View.VISIBLE
            }

        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemRecieveMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.content
            binding.tvTime.text = message.formattedTime

            if (message?.photoUrl != null) {
                Glide.with(binding.imageView)
                    .load(message.photoUrl)
                    .into(binding.imageView)
                binding.imageView.visibility = android.view.View.VISIBLE
                binding.tvMessage.visibility = android.view.View.GONE
            } else {
                binding.imageView.visibility = android.view.View.GONE
                binding.tvMessage.visibility = android.view.View.VISIBLE
            }
        }
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