package com.midterm.chitchatter.ui.contacts

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
import com.midterm.chitchatter.databinding.ItemContactBinding

class ContactAdapter(
    private val contactList: MutableList<Account> = ArrayList(),
    private val listenerDetail: OnItemClickListener,
    private val listenerChat: OnItemClickListener
) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemContactBinding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, listenerDetail, listenerChat)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contactList[position])
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

//    override fun getHeaderId(position: Int): Long {
//        return contactList[position].name[0].code.toLong()
//    }
//
//    override fun onCreateHeaderViewHolder(parent: ViewGroup): HeaderViewHolder {
//        val binding = HeaderContactLayoutBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return HeaderViewHolder(binding)
//    }
//
//    override fun onBindHeaderViewHolder(holder: HeaderViewHolder?, position: Int) {
//        holder?.bind(contactList[position])
//    }

    class ViewHolder(
        private val binding: ItemContactBinding,
        private val listenerDetail: OnItemClickListener,
        private val listenerChat: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Account) {
            binding.tvContactName.text = contact.name
            binding.tvContactEmail.text = contact.email

            if (contact.imageUrl != null) {
                val fileName = contact.imageUrl as String
                val bucketUrl = "gs://chitchatter-b97bf.appspot.com/"

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

                binding.btnDetail.setOnClickListener {
                    listenerDetail.onItemClick(contact)
                }

                binding.btnChat.setOnClickListener {
                    listenerChat.onItemClick(contact)
                }
            } else {
                Glide.with(binding.ivContactAvt)
                    .load("https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?q=80&w=1770&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                    .error(R.drawable.android)
                    .into(binding.ivContactAvt)
            }
        }
    }

    //    class HeaderViewHolder(
//        private val binding: HeaderContactLayoutBinding
//    ) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(contact: Account) {
//            val nameHeader: String = contact.name.substring(0, 1)
//            binding.tvHeaderContact.text = nameHeader
//        }
//    }
    interface OnItemClickListener {
        fun onItemClick(account: Account)
    }

//    private val headerPositions: MutableList<Int> = mutableListOf()

//    init {
//        // Xác định vị trí của các header trong danh sách tài khoản
//        findHeaderPositions()
//    }
//
//     fun findHeaderPositions() {
//        headerPositions.clear()
//        if (contactList.isNotEmpty()) {
//            headerPositions.add(0) // Thêm header cho phần tử đầu tiên
//            var prevHeaderName = contactList[0].name.first()
//
//            for (i in 1 until contactList.size) {
//                val currentHeaderName = contactList[i].name.first()
//                if (currentHeaderName != prevHeaderName) {
//                    headerPositions.add(i) // Thêm header cho phần tử hiện tại
//                    prevHeaderName = currentHeaderName
//                }
//            }
//        }
//    }


//    override fun getHeaderPositionForItem(itemPosition: Int): Int {
//        // Tìm vị trí của header tương ứng với vị trí của phần tử trong danh sách
//        // Bằng cách tìm phần tử trong danh sách vị trí header gần nhất mà không lớn hơn vị trí hiện tại
//        return headerPositions.lastOrNull { it <= itemPosition } ?: RecyclerView.NO_POSITION
//    }
//
//    override fun getHeaderLayout(headerPosition: Int): Int {
//        // Trả về layout resource id cho header
//        return R.layout.header_contact_layout
//    }
//
//    override fun bindHeaderData(header: View, headerPosition: Int) {
//        // Thiết lập dữ liệu cho header
//        val headerTextView = header.findViewById<TextView>(R.id.tv_header_contact)
//        headerTextView.text = contactList[headerPosition].name.first().toString()
//    }
}


