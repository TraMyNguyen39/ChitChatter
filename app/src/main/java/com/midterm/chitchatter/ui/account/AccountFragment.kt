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

class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var progressBar: ProgressBar
    private val args: AccountFragmentArgs by navArgs()
    private val viewModel: AccountViewModel by activityViewModels {
        val repository = (requireActivity().application as ChitChatterApplication).repository
        AccountViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        progressBar = requireActivity().findViewById(R.id.progress_bar_main)
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupActions()

        progressBar.visibility = View.VISIBLE
        viewModel.loadAccountInfo(args.email)
    }

    private fun setupViewModel() {
        viewModel.contact.observe(viewLifecycleOwner) { account ->
            progressBar.visibility = View.GONE
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
                        Glide.with(binding.ivProfileAvt)
                            .load(uri)
                            .error(R.drawable.chitchatter)
                            .into(binding.ivProfileAvt)
                    }.addOnFailureListener { exception ->
                        // Xử lý lỗi nếu có
                        Log.e("FirebaseStorage", "Failed to get download URL", exception)
                    }
                } else {
                    Glide.with(binding.ivProfileAvt)
                        .load("https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?q=80&w=1770&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                        .error(R.drawable.android)
                        .into(binding.ivProfileAvt)
                }
            } else {
                Snackbar.make(requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupActions() {
        binding.btnProfileUnfriend.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Hủy kết bạn")
            builder.setMessage("Bạn có chắc chắn hủy kết bạn với người này?")
            builder.setPositiveButton("Chắc chắn") { dialog, _ ->
                dialog.cancel()
            }
            builder.setNegativeButton("Hủy") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }
    }
}