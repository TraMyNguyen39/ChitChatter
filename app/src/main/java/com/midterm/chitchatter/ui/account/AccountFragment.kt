package com.midterm.chitchatter.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.databinding.FragmentAccountBinding
import com.midterm.chitchatter.utils.ChitChatterUtils
import com.midterm.chitchatter.utils.ContactStatus

class AccountFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var progressBar: ProgressBar
    private val args: AccountFragmentArgs by navArgs()
    private val viewModel: AccountViewModel by activityViewModels {
        val repository = (requireActivity().application as ChitChatterApplication).repository
        AccountViewModelFactory(repository)
    }
    private var contactStatus : Int? = null // contactStatus = null khi họ tự xem profile của mình
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
        contactStatus = if (args.contactStatus != -1) args.contactStatus else null
        setupViews()
        setupViewModel()
        setupActions()
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.VISIBLE
        binding.containerAccountFragment.visibility = View.INVISIBLE
        if (args.email != null) {
            viewModel.loadAccountInfo(userEmail, args.email) //
        } else {
            viewModel.loadAccountInfo(userEmail)
        }
    }

    private fun setupViews() {
        when (contactStatus) {
            ContactStatus.CONNECTED.ordinal -> {
                binding.btnProfileDeny.visibility = View.GONE
                setUpButton1(R.string.txt_unfriend, R.color.black)
            }
            ContactStatus.UNCONNECTED.ordinal -> {
                binding.btnProfileDeny.visibility = View.GONE
                setUpButton1(R.string.txt_action_connect, R.color.primary_color)
            }
            ContactStatus.REQUESTED.ordinal -> {
                binding.btnProfileDeny.visibility = View.GONE
                setUpButton1(R.string.txt_cancel_connect, androidx.appcompat.R.color.material_grey_600)
            }
            ContactStatus.RECEIVED.ordinal -> {
                binding.btnProfileDeny.visibility = View.VISIBLE
                setUpButton1(R.string.txt_accept, R.color.primary_color)
            }
            null -> {
                binding.btnProfileDeny.visibility = View.GONE
                setUpButton1(R.string.txt_edit, R.color.primary_color)
            }
        }
    }

    private fun setUpButton1(textResId: Int, color: Int) {
        binding.btnProfileAction.text = getString(textResId)
        binding.btnProfileAction.setBackgroundColor(requireActivity().getColor(color))
    }

    private fun setupViewModel() {
        viewModel.contact.observe(viewLifecycleOwner) { account ->
            progressBar.visibility = View.GONE
            binding.containerAccountFragment.visibility = View.VISIBLE
            if (account != null) {
                setupProfile(account)
            } else {
                Snackbar.make(requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    private fun setupProfile(account: Account) {
        binding.textProfileDisplayName.text = account.name
        binding.tvProfileEmail.text = account.email
        binding.tvProfileDisplayName.text = account.name
        binding.tvProfileBirthdate.text = account.birthday
        binding.tvProfileGender.text = account.gender
        if (account.contactStatus != null) {
            contactStatus = account.contactStatus
            setupViews()
        }

        if (account.imageUrl != null) {
            val fileName = account.imageUrl as String
            val bucketUrl = "gs://chitchatter-b97bf.appspot.com/avatars/"

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
                .load(R.drawable.chitchatter)
                .error(R.drawable.android).into(binding.ivProfileAvt)
        }
    }

    private fun setupActions() {
        val contactEmail = args.email

        binding.refreshAccount.setOnRefreshListener(this)

        if (contactEmail == null) {
            binding.btnProfileAction.setOnClickListener {
                if (ChitChatterUtils.isOnline(requireContext())) {
                    val action = AccountFragmentDirections.actionAccountFragmentToEditProfileFragment()
                    findNavController().navigate(action)
                } else {
                    Snackbar.make(
                        requireView(), R.string.message_no_internet, Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            return
        }

        binding.btnProfileAction.setOnClickListener {
            when (contactStatus) {
                ContactStatus.CONNECTED.ordinal -> {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Hủy kết nối")
                    builder.setMessage("Bạn có chắc chắn hủy kết bạn với người này?")
                    builder.setPositiveButton("Chắc chắn") { dialog, _ ->
                        dialog.cancel()
                        progressBar.visibility = View.VISIBLE
                        deleteConnect(userEmail!!, contactEmail)
                    }
                    builder.setNegativeButton("Hủy") { dialog, _ ->
                        dialog.cancel()
                    }
                    builder.show()
                }
                ContactStatus.UNCONNECTED.ordinal -> {
                    progressBar.visibility = View.VISIBLE
                    addConnect(userEmail!!, contactEmail)
                }
                ContactStatus.REQUESTED.ordinal -> {
                    progressBar.visibility = View.VISIBLE
                    rejectConnect(userEmail!!, contactEmail)
                }
                ContactStatus.RECEIVED.ordinal -> {
                    progressBar.visibility = View.VISIBLE
                    acceptConnect(userEmail!!, contactEmail)
                }
            }
        }

        binding.btnProfileDeny.setOnClickListener {
            if (contactStatus == ContactStatus.RECEIVED.ordinal) {
                progressBar.visibility = View.VISIBLE
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
                setupViews()
            } else {
                Snackbar.make(
                    requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                ).show()
            }
            progressBar.visibility = View.GONE
        }
    }

    private fun deleteConnect(userEmail: String, contactEmail: String) {
        viewModel.deleteContact(userEmail, contactEmail) { isSuccessful ->
            if (isSuccessful) {
                contactStatus = ContactStatus.UNCONNECTED.ordinal
                Snackbar.make(
                    requireView(), "Đã hủy kết bạn!", Snackbar.LENGTH_LONG
                ).show()
                setupViews()
            } else {
                Snackbar.make(
                    requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                ).show()
            }
            progressBar.visibility = View.GONE
        }
    }

    private fun acceptConnect(userEmail: String, contactEmail: String) {
        viewModel.acceptContact(userEmail, contactEmail) { isSuccessful ->
            if (isSuccessful) {
                contactStatus = ContactStatus.CONNECTED.ordinal
                Snackbar.make(
                    requireView(), "Bạn đã kết nối với $contactEmail", Snackbar.LENGTH_LONG
                ).show()
                setupViews()
            } else {
                Snackbar.make(
                    requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                ).show()
            }
            progressBar.visibility = View.GONE
        }
    }
    private fun rejectConnect(userEmail: String, contactEmail: String) {
        viewModel.rejectContact(userEmail, contactEmail) { isSuccessful ->
            if (isSuccessful) {
                contactStatus = ContactStatus.UNCONNECTED.ordinal
                setupViews()
            } else {
                Snackbar.make(
                    requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                ).show()
            }
            progressBar.visibility = View.GONE
        }
    }

    override fun onRefresh() {
        progressBar.visibility = View.VISIBLE
        binding.containerAccountFragment.visibility = View.INVISIBLE
        if (contactStatus != null) {
            viewModel.loadAccountInfo(userEmail, args.email)
        } else {
            viewModel.loadAccountInfo(userEmail)
        }
        binding.refreshAccount.isRefreshing = false
    }
}