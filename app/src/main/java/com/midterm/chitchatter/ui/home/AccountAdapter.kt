package com.midterm.chitchatter.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
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
            val url = if (!lastMsg?.url.isNullOrEmpty()) {
                lastMsg?.url
            } else {
                "https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?q=80&w=1770&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            }
            Glide.with(binding.ivSender)
                .load(url)
                .error(R.drawable.android)
                .into(binding.ivSender)

            Glide.with(binding.ivReceiver)
                .load(url)
                .error(R.drawable.android)
                .into(binding.ivReceiver)

            binding.tvName.text = lastMsg?.name
            if (lastMsg != null) {
                if (lastMsg.isIncoming) {
                    binding.tvMessage.text = lastMsg.content
                }
                else {
                    binding.tvMessage.text = "You: ${lastMsg.content}"
                }
            } else {
                binding.tvMessage.text = ""
            }

            binding.tvTime.text = lastMsg?.createdAt

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

    fun updateMessage(messages: List<Message>) {
        var cnt = 0;
        if (messagesList.isEmpty()) {
            messagesList = (messages as ArrayList)
        }
        for (message in messages) {
            updateMessage(message, cnt)
            cnt++
        }
    }

    fun updateMessage(message: Message, position: Int) {
        val contactUsername = message.name
        messagesList[position] = message
        notifyItemChanged(position)
    }

    private fun hashCode(sender: String, receiver: String) : Int {
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
        fun onItemClick(account: Account)
    }

}