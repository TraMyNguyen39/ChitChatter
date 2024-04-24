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
    private val accounts: MutableList<Account> = mutableListOf(),
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    private val messageMap = mutableMapOf<Int, Message>()

    class ViewHolder(
        private val binding: ItemChatboxBinding,
        private val listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(account: Account, lastMsg: Message?) {
            Glide.with(binding.ivSender)
                .load(account.imageUrl)
                .error(R.drawable.android)
                .into(binding.ivSender)

            Glide.with(binding.ivReceiver)
                .load(account.imageUrl)
                .error(R.drawable.android)
                .into(binding.ivReceiver)

            binding.tvName.text = account.name
            if (lastMsg != null) {
               if (lastMsg.isIncoming) {
                   binding.tvMessage.text = lastMsg.data.text
               }
                else {
                    binding.tvMessage.text = "You: ${lastMsg.data.text}"
               }
            } else {
                binding.tvMessage.text = ""
            }

            binding.root.setOnClickListener {
                listener.onItemClick(account)
            }
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
        return accounts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contactAccount: Account = accounts[position]
        val currentAccount = HomeViewModel.currentAccount.value?.username!!
        val lastMsg = messageMap[hashCode(currentAccount, contactAccount.username)]
        holder.bind(contactAccount, lastMsg)
    }

    fun updateMessage(messages: Set<Message>) {
        for (message in messages) {
            updateMessage(message)
        }
    }

    fun updateMessage(message: Message) {
        val currentUsername = HomeViewModel.currentAccount.value?.username!!
        // Người đang nhắn
        val contactUsername = if (currentUsername == message.sender) {
            message.receiver
        } else {
            message.sender
        }
        val hashValue = hashCode(currentUsername, contactUsername)
        messageMap[hashValue] = message
        val positionHasChanged = accounts.indexOf(Account(contactUsername))
        notifyItemChanged(positionHasChanged)
    }

    private fun hashCode(sender: String, receiver: String) : Int {
        var result = sender.hashCode()
        result = result * 31 + receiver.hashCode()
        return result
    }

    fun updateAccounts(accounts: List<Account>) {
        val oldSize = this.accounts.size
        val newSize = accounts.size
        this.accounts.clear()
        this.accounts.addAll(accounts)
        if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        } else {
            notifyItemRangeRemoved(newSize - 1, oldSize - newSize)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(account: Account)
    }

}