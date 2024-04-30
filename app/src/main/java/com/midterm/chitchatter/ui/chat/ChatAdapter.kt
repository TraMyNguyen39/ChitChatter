package com.midterm.chitchatter.ui.chat

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.transition.Transition
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.databinding.ItemMessageBinding

class ChatAdapter(
    context: Context,
    private val imageUrl: String?,
    private val onPhotoClicked: (photo: Uri) -> Unit
) : ListAdapter<Message, ChatAdapter.MessageViewHolder>(DIFF_CALLBACK) {

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

    class MessageViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
    ) {
        val binding: ItemMessageBinding = ItemMessageBinding.bind(itemView)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val holder = MessageViewHolder(parent)
        holder.binding.textMessage.setOnClickListener {
            val photo = it.getTag(R.id.tag_photo) as Uri?
            if (photo != null) {
                onPhotoClicked(photo)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        val layoutParam = holder.binding.textMessage.layoutParams as FrameLayout.LayoutParams
        if (message.isIncoming) {
            holder.binding.textMessage.run {
                setBackgroundResource(R.drawable.message_incoming)
                ViewCompat.setBackgroundTintList(this, tint.incoming)
                setPadding(
                    padding.horizontalLong, padding.vertical,
                    padding.horizontalShort, padding.vertical
                )
                layoutParams = layoutParam.apply {
                    gravity = Gravity.START
                }
                Glide.with(holder.binding.imageMessageItem)
                    .load(imageUrl)
                    .circleCrop()
                    .error(R.drawable.profile)
                    .into(holder.binding.imageMessageItem)
            }
        } else {
            holder.binding.textMessage.run {
                setBackgroundResource(R.drawable.message_outgoing)
                ViewCompat.setBackgroundTintList(this, tint.outgoing)
                setPadding(
                    padding.horizontalShort, padding.vertical,
                    padding.horizontalLong, padding.vertical
                )
                layoutParams = layoutParam.apply {
                    gravity = Gravity.END
                }
            }
        }

        if (message.data.photoUrl != null) {
            val photoUri = message.data.photoUrl
            holder.binding.textMessage.setTag(R.id.tag_photo, photoUri)
            Glide.with(holder.binding.textMessage)
                .load(photoUri)
                .error(R.drawable.missing)
                .into(CompoundBottomTarget(holder.binding.textMessage, photoSize, photoSize))
        } else {
            holder.binding.textMessage.setTag(R.id.tag_photo, null)
            holder.binding.textMessage.setCompoundDrawables(null, null, null, null)
        }
        holder.binding.textMessage.text = message.data.text
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