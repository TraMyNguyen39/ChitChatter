package com.midterm.chitchatter.ui.edit_profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.databinding.FragmentEditProfileBinding
import com.midterm.chitchatter.utils.ChitChatterUtils
import com.midterm.chitchatter.utils.ChitChatterUtils.afterTextChanged


class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var progressBar: ProgressBar
    private val viewModel: EditProfileViewModel by activityViewModels {
        val repository = (requireActivity().application as ChitChatterApplication).repository
        EditProfileViewModelFactory(repository)
    }
    private var userEmail: String? = null
    private var formChanged: Boolean = false
    private var fileName: String? = null
    private var uri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageFromGallery()
        } else {
            showMessage(R.string.camera_denied, Snackbar.LENGTH_LONG)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        StartActivityForResult(), this::handleResult
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userEmail = ChitChatterUtils.getCurrentAccount(requireContext())!!
        if (userEmail == null) {
            requireActivity().finish()
        }

        progressBar = requireActivity().findViewById(R.id.progress_bar_main)
        binding = FragmentEditProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupViewModel()
        setupActions()
    }

    override fun onPause() {
        super.onPause()
        viewModel.resetState()
    }

    private fun setupViews() {
        progressBar.visibility = View.VISIBLE
        binding.containerEditProfileFragment.visibility = View.INVISIBLE
        viewModel.loadAccountInfo(userEmail!!)
    }

    private fun setupViewModel() {
        viewModel.contact.observe(viewLifecycleOwner) { account ->
            progressBar.visibility = View.GONE
            binding.containerEditProfileFragment.visibility = View.VISIBLE
            if (account != null) {
                setupProfile(account)
            } else {
                Snackbar.make(requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.updateMessage.observe(viewLifecycleOwner) {
            progressBar.visibility = View.GONE
            if (it == R.string.txt_update_profile_success) {
                val displayName = binding.editProfileDisplayName.text.toString()
                binding.textProfileDisplayName.text = displayName
            } else if (it == R.string.txt_update_avatar_success) {
                // Lưu thành công vào firestore -> cập nhật preference
                val sharedPref = requireActivity().getSharedPreferences(
                    getString(R.string.preference_account_key), Context.MODE_PRIVATE)

                with (sharedPref.edit()) {
                    putString(getString(R.string.preference_avt_url_key), fileName)
                    apply()
                }
                // Cập nhật avt trên toolbar
                if (uri != null) {
                    val avtImageView = requireActivity().findViewById<ImageView>(R.id.iv_avatar_id)
                    avtImageView.setImageURI(uri)
                }
            }
            if (it != null) {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupActions() {
        binding.btnProfileSave.setOnClickListener {
            ChitChatterUtils.hideKeyBoard(requireView())
            if (formChanged) {
                val displayName = binding.editProfileDisplayName.text.toString()
                val birthdate = binding.editProfileBirthdate.text.toString()
                val gender = if (binding.radioBtnMale.isChecked) "Nam" else "Nữ"

                val isFormCorrect = viewModel.checkFormState(displayName, birthdate)
                if (isFormCorrect) {
                    progressBar.visibility = View.VISIBLE
                    viewModel.updateProfile(userEmail!!, displayName, birthdate, gender)
                } else {
                    Snackbar.make(
                        requireView(),
                        "Vui lòng nhập thông tin đúng định dạng",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(
                    requireView(), "Bạn chưa nhập thay đổi nào", Snackbar.LENGTH_LONG
                ).show()
            }
        }
        binding.btnChangeAvt.setOnClickListener {
            // Kiểm tra quyền truy cập
            when {
                ContextCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    pickImageFromGallery()
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) -> {
                    showMessage(R.string.camera_permission_prompt, Snackbar.LENGTH_LONG, true)
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
        binding.editProfileDisplayName.afterTextChanged {
            formChanged = true
        }
        binding.editProfileBirthdate.afterTextChanged {
            formChanged = true
        }
        binding.radioBtnMale.setOnCheckedChangeListener { _, _ ->
            formChanged = true
        }
        binding.radioBtnFemale.setOnCheckedChangeListener { _, _ ->
            formChanged = true
        }
    }
    private fun handleResult(activityResult: ActivityResult?) {
        if (activityResult?.resultCode == Activity.RESULT_OK) {
            uri = activityResult.data?.data
            if (uri != null) {
                progressBar.visibility = View.VISIBLE
                fileName = getFileName(uri!!)
                // Lưu lên Firebase Storage
                val storageRef = Firebase.storage.reference
                val uploadTask = storageRef.child("avatars/${fileName}").putFile(uri!!)
                uploadTask.addOnSuccessListener {
                    // Tiếp tục cập nhật địa chỉ avt vào firestore
                    viewModel.updateAvatar(userEmail!!, fileName!!)
                }.addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Snackbar.make(requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG).show()
                }

                binding.ivProfileAvt.setImageURI(uri)
            }
        }
    }
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }
        return fileName
    }
    private fun pickImageFromGallery() {
        // Intent để chọn ảnh từ gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        cameraLauncher.launch(intent)
    }

    private fun showMessage(messageId: Int, duration: Int, showAction: Boolean = false) {
        val snackBar = Snackbar.make(binding.root, messageId, duration)
        if (showAction && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            snackBar.setAction("OK") {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            snackBar.setAction("No thank") {
                showMessage(R.string.camera_permission_denied, Snackbar.LENGTH_LONG)
            }
        }
        snackBar.show()
    }

    private fun setupProfile(account: Account) {
        binding.textProfileDisplayName.text = account.name
        binding.editProfileEmail.setText(account.email)
        binding.editProfileDisplayName.setText(account.name)
        binding.editProfileBirthdate.setText(account.birthday)
        if (account.gender == "Nữ") {
            binding.radioBtnFemale.isChecked = true
        } else {
            binding.radioBtnMale.isChecked = true
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
}