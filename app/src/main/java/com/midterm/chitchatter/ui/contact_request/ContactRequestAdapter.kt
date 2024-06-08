package com.midterm.chitchatter.ui.contact_request

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.ContactRequestSender
import com.midterm.chitchatter.databinding.ItemRequestContactBinding

class ContactRequestAdapter(
    private val requestSenders: MutableList<ContactRequestSender> = ArrayList(),
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ContactRequestAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRequestContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(requestSenders[position])
    }

    override fun getItemCount(): Int {
        return requestSenders.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(contactRequests: List<ContactRequestSender>?) {
        if (contactRequests != null) {
            this.requestSenders.clear()
            this.requestSenders.addAll(contactRequests)
            notifyDataSetChanged()
        }
    }

//    @SuppressLint("NotifyDataSetChanged")
//    fun deleteRequestSender(contactRequest: ContactRequestSender) {
//        requestSenders.remove(contactRequest)
//        notifyDataSetChanged()
//    }

    class ViewHolder(
        private val binding: ItemRequestContactBinding,
        private val listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(requestSender: ContactRequestSender) {
            binding.tvContactName.text = requestSender.displayName
            binding.tvContactEmail.text = requestSender.email
            binding.textTime.text = requestSender.time

            if (requestSender.imageUrl != null) {
                val fileName = requestSender.imageUrl as String
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


            binding.btnAccept.setOnClickListener {
                listener.onAcceptItemClick(requestSender)
            }

            binding.btnDeny.setOnClickListener {
                listener.onRejectItemClick(requestSender)
            }

            itemView.setOnClickListener {
                listener.onDetailItemClick(requestSender)
            }
        }
    }

    interface OnItemClickListener {
        fun onDetailItemClick(requestSender: ContactRequestSender)
        fun onAcceptItemClick(requestSender: ContactRequestSender)
        fun onRejectItemClick(requestSender: ContactRequestSender)
    }
}