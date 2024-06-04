package com.midterm.chitchatter.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.databinding.FragmentAccountBinding
import com.midterm.chitchatter.utils.ChitChatterUtils
import com.midterm.chitchatter.utils.ContactStatus

class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var progressBar: ProgressBar
    private val args: AccountFragmentArgs by navArgs()
    private val viewModel: AccountViewModel by activityViewModels {
        val repository = (requireActivity().application as ChitChatterApplication).repository
        AccountViewModelFactory(repository)
    }
    private var contactStatus = ContactStatus.CONNECTED.ordinal
    private var userEmail : String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        userEmail = ChitChatterUtils.getCurrentAccount(requireContext())!!
        if (userEmail == null) {
            requireActivity().finish()
        }

        progressBar = requireActivity().findViewById(R.id.progress_bar_main)
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupViewModel()
        setupActions()
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.VISIBLE
        binding.containerAccountFragment.visibility = View.INVISIBLE
        viewModel.loadAccountInfo(args.email)
    }

    private fun setupViews() {
        contactStatus = args.contactStatus
        when (contactStatus) {
            ContactStatus.CONNECTED.ordinal -> {
                setUpButton(R.string.txt_unfriend, R.color.black)
            }
            ContactStatus.UNCONNECTED.ordinal -> {
                setUpButton(R.string.txt_action_connect, R.color.primary_color)
            }
            ContactStatus.REQUESTED.ordinal -> {
                setUpButton(R.string.txt_cancel_connect, androidx.appcompat.R.color.material_grey_600)
            }
            else -> {
                binding.btnProfileUnfriend.visibility = View.VISIBLE
                setUpButton(R.string.txt_accept, R.color.primary_color)
            }
        }
    }

    private fun setUpButton(textResId: Int, color: Int) {
        binding.btnProfileAction.text = getString(textResId)
        binding.btnProfileAction.setBackgroundColor(requireActivity().getColor(color))
    }

    private fun setupViewModel() {
        viewModel.contact.observe(viewLifecycleOwner) { account ->
            progressBar.visibility = View.GONE
            binding.containerAccountFragment.visibility = View.VISIBLE
            if (account != null) {
                binding.textProfileDisplayName.text = account.name
                binding.tvProfileEmail.text = account.email
                binding.tvProfileDisplayName.text = account.name
                binding.tvProfileBirthdate.text = account.birthday
                binding.tvProfileGender.text = account.gender

                if (account.imageUrl != null) {
                    val fileName = account.imageUrl as String
                    val bucketUrl = "gs://chitchatter-b97bf.appspot.com/"

                    val storage: FirebaseStorage = FirebaseStorage.getInstance()
                    val storageRef: StorageReference = storage.getReferenceFromUrl(bucketUrl)
                    val imageRef: StorageReference = storageRef.child(fileName)
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(binding.ivProfileAvt).load(uri).error(R.drawable.chitchatter)
                            .into(binding.ivProfileAvt)
                    }.addOnFailureListener { exception ->
                        // Xử lý lỗi nếu có
                        Log.e("FirebaseStorage", "Failed to get download URL", exception)
                    }
                } else {
                    Glide.with(binding.ivProfileAvt)
                        .load("https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?q=80&w=1770&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                        .error(R.drawable.android).into(binding.ivProfileAvt)
                }
            } else {
                Snackbar.make(requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupActions() {
        val contactEmail = args.email
        binding.btnProfileAction.setOnClickListener {
            when (contactStatus) {
                ContactStatus.CONNECTED.ordinal -> {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Hủy kết nối")
                    builder.setMessage("Bạn có chắc chắn hủy kết bạn với người này?")
                    builder.setPositiveButton("Chắc chắn") { dialog, _ ->
                        dialog.cancel()
                        deleteConnect(userEmail!!, contactEmail)
                    }
                    builder.setNegativeButton("Hủy") { dialog, _ ->
                        dialog.cancel()
                    }
                    builder.show()
                }
                ContactStatus.UNCONNECTED.ordinal -> {
                    addConnect(userEmail!!, contactEmail)
                }
                ContactStatus.REQUESTED.ordinal -> {
                    rejectConnect(userEmail!!, contactEmail)
                }
                else -> {
                    acceptConnect(userEmail!!, contactEmail)
                }
            }
        }

        binding.btnProfileUnfriend.setOnClickListener {
            if (contactStatus == ContactStatus.RECEIVED.ordinal) {
                rejectConnect(userEmail!!, contactEmail)
            }
        }
    }

    private fun addConnect(userEmail: String, contactEmail: String) {
        viewModel.addContact(userEmail, contactEmail) { isSuccessful ->
            if (isSuccessful) {
                contactStatus = ContactStatus.REQUESTED.ordinal
                Snackbar.make(
                    requireView(), "Đã gửi lời mời kết bạn!", Snackbar.LENGTH_LONG
                ).show()
                setUpButton(R.string.txt_cancel_connect, androidx.appcompat.R.color.material_grey_600)
            } else {
                Snackbar.make(
                    requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun deleteConnect(userEmail: String, contactEmail: String) {
        viewModel.deleteContact(userEmail, contactEmail) { isSuccessful ->
            if (isSuccessful) {
                contactStatus = ContactStatus.UNCONNECTED.ordinal
                setUpButton(R.string.txt_action_connect, R.color.primary_color)
            } else {
                Snackbar.make(
                    requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun acceptConnect(userEmail: String, contactEmail: String) {
        viewModel.deleteContact(userEmail, contactEmail) { isSuccessful ->
            if (isSuccessful) {
                contactStatus = ContactStatus.UNCONNECTED.ordinal
                setUpButton(R.string.txt_action_connect, R.color.primary_color)
            } else {
                Snackbar.make(
                    requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun rejectConnect(userEmail: String, contactEmail: String) {
        viewModel.deleteContact(userEmail, contactEmail) { isSuccessful ->
            if (isSuccessful) {
                contactStatus = ContactStatus.UNCONNECTED.ordinal
                setUpButton(R.string.txt_action_connect, R.color.primary_color)
            } else {
                Snackbar.make(
                    requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}