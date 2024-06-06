package com.midterm.chitchatter.ui.contacts

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.*
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.databinding.ItemAddContactBinding
import com.midterm.chitchatter.databinding.ItemContactBinding
import com.midterm.chitchatter.databinding.ItemRequestBinding
import com.midterm.chitchatter.utils.ContactStatus

class ContactAdapter(
    private val contactList: MutableList<Account> = ArrayList(),
    private val listener: OnItemClickListener
) : Adapter<ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (contactList[position].contactStatus) {
            ContactStatus.CONNECTED.ordinal -> 1
            ContactStatus.RECEIVED.ordinal -> 2
            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> {
                val binding: ItemContactBinding =
                    ItemContactBinding.inflate(inflater, parent, false)
                ContactViewHolder(binding, listener)
            }

            0 -> {
                val binding: ItemAddContactBinding =
                    ItemAddContactBinding.inflate(inflater, parent, false)
                AddOrPendingContactViewHolder(binding, listener)
            }

            else -> {
                val binding: ItemRequestBinding =
                    ItemRequestBinding.inflate(inflater, parent, false)
                ContactRequestViewHolder(binding, listener)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ContactViewHolder -> holder.bind(contactList[position])
            is AddOrPendingContactViewHolder -> {
                holder.bind(contactList[position])
                holder.binding.btnAddContact.setOnClickListener {
                    val contact = contactList[position]
                    if (contact.contactStatus == ContactStatus.UNCONNECTED.ordinal) {
                        listener.onAddItemClick(contact)
                    } else {
                        listener.onRejectItemClick(contact)
                    }
                }
            }

            is ContactRequestViewHolder -> {
                holder.bind(contactList[position])
                holder.binding.btnContactAccept.setOnClickListener {
                    val contact = contactList[position]
                    listener.onAcceptItemClick(contact)
                }
                holder.binding.btnContactDeny.setOnClickListener {
                    val contact = contactList[position]
                    listener.onRejectItemClick(contact)
                }
            }
        }
    }

    fun notifyItemChange(account: Account) {
        val index = contactList.indexOf(account)
        if (index > -1) {
            contactList[index] = account
            notifyItemChanged(index)
        }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(contactList: List<Account>?) {
        if (contactList != null) {
            this.contactList.clear()
            this.contactList.addAll(contactList)
            notifyDataSetChanged()
        }
    }

    class ContactViewHolder(
        private val binding: ItemContactBinding,
        private val listener: OnItemClickListener
    ) : ViewHolder(binding.root) {
        fun bind(contact: Account) {
            binding.tvContactName.text = contact.name
            binding.tvContactEmail.text = contact.email

            if (contact.imageUrl != null) {
                val fileName = contact.imageUrl as String
                val bucketUrl = "gs://chitchatter-b97bf.appspot.com/avatars/"

                val storage: FirebaseStorage = FirebaseStorage.getInstance()
                val storageRef: StorageReference = storage.getReferenceFromUrl(bucketUrl)
                val imageRef: StorageReference = storageRef.child(fileName)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(binding.ivContactAvt)
                        .load(uri)
                        .error(R.drawable.chitchatter)
                        .into(binding.ivContactAvt)
                }.addOnFailureListener { exception ->
                    // Xử lý lỗi nếu có
                    Log.e("FirebaseStorage", "Failed to get download URL", exception)
                }
            } else {
                Glide.with(binding.ivContactAvt)
                    .load("https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?q=80&w=1770&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                    .error(R.drawable.android)
                    .into(binding.ivContactAvt)
            }


            binding.btnDetail.setOnClickListener {
                listener.onDetailItemClick(contact)
            }

            binding.btnChat.setOnClickListener {
                listener.onChatItemClick(contact)
            }

            itemView.setOnClickListener {
                listener.onDetailItemClick(contact)
            }
        }
    }

    class ContactRequestViewHolder(
        val binding: ItemRequestBinding,
        private val listener: OnItemClickListener
    ) : ViewHolder(binding.root) {
        fun bind(contact: Account) {
            binding.tvContactName.text = contact.name
            binding.tvContactEmail.text = contact.email

            if (contact.imageUrl != null) {
                val fileName = contact.imageUrl as String
                val bucketUrl = "gs://chitchatter-b97bf.appspot.com/avatars/"

                val storage: FirebaseStorage = FirebaseStorage.getInstance()
                val storageRef: StorageReference = storage.getReferenceFromUrl(bucketUrl)
                val imageRef: StorageReference = storageRef.child(fileName)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(binding.ivContactAvt)
                        .load(uri)
                        .error(R.drawable.chitchatter)
                        .into(binding.ivContactAvt)
                }.addOnFailureListener { exception ->
                    // Xử lý lỗi nếu có
                    Log.e("FirebaseStorage", "Failed to get download URL", exception)
                }
            } else {
                Glide.with(binding.ivContactAvt)
                    .load(R.drawable.chitchatter)
                    .error(R.drawable.android)
                    .into(binding.ivContactAvt)
            }

            itemView.setOnClickListener {
                listener.onDetailItemClick(contact)
            }
        }
    }

    class AddOrPendingContactViewHolder(
        val binding: ItemAddContactBinding,
        private val listener: OnItemClickListener
    ) : ViewHolder(binding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(contact: Account) {
            binding.tvContactName.text = contact.name
            binding.tvContactEmail.text = contact.email
            if (contact.contactStatus == ContactStatus.REQUESTED.ordinal) {
                binding.btnAddContact.text = "Hủy"
                binding.btnAddContact.isActivated = false
                binding.btnAddContact.setBackgroundColor(itemView.context.getColor(R.color.black))
            } else {
                binding.btnAddContact.text = "Kết nối"
                binding.btnAddContact.setBackgroundColor(itemView.context.getColor(R.color.primary_color))
            }

            if (contact.imageUrl != null) {
                val fileName = contact.imageUrl as String
                val bucketUrl = "gs://chitchatter-b97bf.appspot.com/avatars/"

                val storage: FirebaseStorage = FirebaseStorage.getInstance()
                val storageRef: StorageReference = storage.getReferenceFromUrl(bucketUrl)
                val imageRef: StorageReference = storageRef.child(fileName)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(binding.ivContactAvt)
                        .load(uri)
                        .error(R.drawable.chitchatter)
                        .into(binding.ivContactAvt)
                }.addOnFailureListener { exception ->
                    // Xử lý lỗi nếu có
                    Log.e("FirebaseStorage", "Failed to get download URL", exception)
                }
            } else {
                Glide.with(binding.ivContactAvt)
                    .load("https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?q=80&w=1770&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                    .error(R.drawable.android)
                    .into(binding.ivContactAvt)
            }

            itemView.setOnClickListener {
                listener.onDetailItemClick(contact)
            }
        }
    }

    interface OnItemClickListener {
        fun onDetailItemClick(account: Account)
        fun onChatItemClick(account: Account)
        fun onAddItemClick(account: Account)
//        fun onRemoveItemClick(account: Account)
        fun onAcceptItemClick(account: Account)
        fun onRejectItemClick(account: Account)
    }
}


