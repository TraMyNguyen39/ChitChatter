package com.midterm.chitchatter.ui.chat

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.ViewCompat

typealias OnImageAddedListener = (contentUri: Uri, mimeType: String, label: String) -> Unit

private val SupportMimeType = arrayOf(
    "image/jpg", "image/png", "image/gif"
)

class ChatEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private var onImageAddedListener: OnImageAddedListener? = null

    init {
        ViewCompat.setOnReceiveContentListener(this, SupportMimeType) { _, payload ->
            val (content, remaining) = payload.partition { it.uri != null }
            if (content != null) {
                val clip = content.clip
                val mimeType = SupportMimeType.find {
                    clip.description.hasMimeType(it)
                }
                if (mimeType != null && clip.itemCount > 0) {
                    onImageAddedListener?.invoke(
                        clip.getItemAt(0).uri,
                        mimeType,
                        clip.description.label.toString()
                    )
                }
            }
            remaining
        }
    }

    fun setOnImageAddedListener(listener: OnImageAddedListener?) {
        onImageAddedListener = listener
    }
}