package com.midterm.chitchatter.ui.chat

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.databinding.ItemRecieveMessageBinding
import com.midterm.chitchatter.databinding.ItemContainerSentMessageBinding

class ChatAdapter(
    context: Context,
    private val imageUrl: String?,
    private val onPhotoClicked: (photo: Uri) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private val tint = object {
        val incoming: ColorStateList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.primary_color)
        )
        val outgoing: ColorStateList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.dark_gray)
        )
    }
    private val padding = object {
        val vertical: Int = context.resources.getDimensionPixelSize(
            R.dimen.message_padding_vertical
        )
        val horizontalShort: Int = context.resources.getDimensionPixelSize(
            R.dimen.message_padding_horizontal_short
        )
        val horizontalLong: Int = context.resources.getDimensionPixelSize(
            R.dimen.message_padding_horizontal_long
        )
    }
    private val photoSize = context.resources.getDimensionPixelSize(R.dimen.photo_size)

    inner class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            // Bind data to the views in the SentMessage layout
            binding.tvMessage.text = message.data.text
            binding.tvTime.text = message.formattedTime

            binding.tvMessage.setBackgroundResource(R.drawable.message_outgoing)
            binding.tvMessage.setPadding(
                padding.horizontalShort, padding.vertical,
                padding.horizontalLong, padding.vertical
            )
            // Load the image if it exists
//                message.data.photoUrl?.let { photoUrl ->
//                Log.d("ChatAdapter", "photoUrl: $message.data.photoUrl")
//                Glide.with(binding.imageView)
//                    .load(photoUrl)
//                    .error(R.drawable.missing)
//                    .into(CompoundBottomTarget(binding.tvMessage, photoSize, photoSize))
//                binding.imageView.visibility = View.VISIBLE
//                binding.tvMessage.visibility = View.GONE
            Log.d("ChatAdapter", "photoUrl: ${message.data.photoUrl}")
            if (message.data.photoUrl != null) {
                binding.imageView.visibility = View.VISIBLE
                binding.tvMessage.visibility = View.GONE
                Glide.with(binding.imageView)
                    .load(message.data.photoUrl)
                    .error(R.drawable.missing)
                    .into(binding.imageView)
                binding.imageView.setOnClickListener {
                    onPhotoClicked(Uri.parse(message.data.photoUrl))
                }
            } else {
                binding.imageView.visibility = View.GONE
                binding.tvMessage.visibility = View.VISIBLE
            }
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemRecieveMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            // Bind data to the views in the ReceivedMessage layout
            binding.tvMessage.text = message.data.text
            binding.tvTime.text = message.formattedTime

            // Set the background and padding for the message
            binding.tvMessage.setBackgroundResource(R.drawable.message_incoming)
//            binding.tvMessage.setPadding(
//                padding.horizontalLong, padding.vertical,
//                padding.horizontalShort, padding.vertical
//            )
            // Load the profile image
            Glide.with(binding.imageView)
                .load(imageUrl)
                .circleCrop()
                .error(R.drawable.profile)
                .into(binding.imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_INCOMING) {
            Log.d("ChatAdapter", "onCreateViewHolder with item_container_message: ")
            R.layout.item_container_sent_message
        } else {
            Log.d("ChatAdapter", "onCreateViewHolder with item_container_sent_message: ")
            R.layout.item_container_sent_message
        }
        val itemView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return if (viewType == VIEW_TYPE_INCOMING) {
            ReceivedMessageViewHolder(ItemRecieveMessageBinding.bind(itemView))
        } else {
            SentMessageViewHolder(ItemContainerSentMessageBinding.bind(itemView))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        Log.d("ChatAdapter", "onBindViewHolder: $message")
        Log.d("ChatAdapter","isIncoming: ${message.isIncoming}")
        if (message.isIncoming) {
            // Bind data for received message
            (holder as ReceivedMessageViewHolder).bind(message)
        } else {
            // Bind data for sent message
            (holder as SentMessageViewHolder).bind(message)
//            val lastMessage = message.value?.last() // Lấy message cuối cùng từ danh sách
            val photoUrl = message?.data?.photoUrl // Lấy photoUrl từ message

            if (photoUrl != null) {
                val imageView = holder.itemView.findViewById<ImageView>(R.id.imageView)
                imageView.visibility = View.VISIBLE

                Glide.with(holder.itemView)
                    .load(photoUrl)
                    .into(imageView)
                Log.d("ChatAdapter", "photoUrl load success: $photoUrl")
            }
            else {
                val imageView = holder.itemView.findViewById<ImageView>(R.id.imageView)
                imageView.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isIncoming) {
            VIEW_TYPE_INCOMING
        } else {
            VIEW_TYPE_OUTGOING
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        Log.d("ChatAdapter", "Adapter has been attached to RecyclerView")

    }

    companion object {
        private const val VIEW_TYPE_INCOMING = 1
        private const val VIEW_TYPE_OUTGOING = 2
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

}

private class CompoundBottomTarget(
    private val view: TextView,
    width: Int,
    height: Int
) : CustomTarget<Drawable>(width, height) {
    override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, resource)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null, null, null, placeholder
        )
    }

}
