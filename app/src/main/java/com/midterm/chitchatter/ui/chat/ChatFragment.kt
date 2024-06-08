package com.midterm.chitchatter.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.adapter.MessageAdapter
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.databinding.FragmentChatBinding
import com.midterm.chitchatter.ui.MainActivity
import com.midterm.chitchatter.utils.ChitChatterUtils
import java.io.ByteArrayOutputStream

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var chatViewModel: ChatViewModel
//    private lateinit var chatAdapter: ChatAdapter
    private var email: String? = null
    private var senderEmail: String? = null
    private var receiverEmail: String? = null
    private var displayName: String? = null
    private var avtUrl: String? = null
    private var token: String? = null
    private lateinit var viewModelFactory: ChatViewModelFactory

    private var photoUri: Uri? = null

    private val REQUEST_CAMERA_PERMISSION = 200
    private val REQUEST_GALLERY_PERMISSION = 300
    private val REQUEST_CODE_PICK_IMAGE = 100
    val REQUEST_CODE_CAPTURE_IMAGE = 101

    private val requestGalleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageFromGallery()
        } else {
            showMessage(R.string.gallery_denied, Snackbar.LENGTH_LONG)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageFromCamera()
        } else {
            showMessage(R.string.gallery_denied, Snackbar.LENGTH_LONG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val messageJson = arguments?.getString(ARG_MESSAGE_JSON)
        val gson = Gson()

        val message = try {
            gson.fromJson(messageJson, Message::class.java)
        } catch (e: Exception) {
            val message = Message()
            message.isIncoming = false
            message
        }
        token = arguments?.getString(ARG_TOKEN)
        Log.d("ChatFragment", "token: $token")
        email = arguments?.getString(ARG_EMAIL)
        senderEmail = arguments?.getString(ARG_SENDER_EMAIL)
        receiverEmail = arguments?.getString(ARG_RECEIVER_EMAIL)
        displayName = arguments?.getString(ARG_DISPLAY_NAME)
        avtUrl = arguments?.getString(ARG_AVT_URL)
        val repository = (requireActivity().application as ChitChatterApplication).repository


        viewModelFactory = ChatViewModelFactory(repository, senderEmail, receiverEmail)

        chatViewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]
        chatViewModel.updateInteractingAccount(Account(email = receiverEmail ?: ""))

//        viewModelFactory = ChatViewModelFactory(repository)
//        chatViewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]
        if (senderEmail != null && receiverEmail != null) {
            chatViewModel = ViewModelProvider(
                this, ChatViewModelFactory(repository, senderEmail, receiverEmail)
            ).get(ChatViewModel::class.java)
        } else {
            Log.d("ChatFragment", "senderEmail: $senderEmail, receiverEmail: $receiverEmail")
//            chatViewModel = ViewModelProvider(this, ChatViewModelFactory(repository, email)).get(ChatViewModel::class.java)
        }

        val interactingAccountEmail = if (message?.isIncoming == true) receiverEmail else senderEmail
        Log.d("ChatFragment", "interactingAccountEmail: $interactingAccountEmail")
        chatViewModel.updateInteractingAccount(Account(email = interactingAccountEmail ?: ""))
        chatViewModel.updateInteractingAccountToken(
            Account(
                email = interactingAccountEmail ?: ""
            ).token ?: ""
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        progressBar = requireActivity().findViewById(R.id.progress_bar_main)
        progressBar.visibility = View.VISIBLE
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)
        hideNavigationView()
        setupViewModel()
        setupViews()
        setupActions()
        setupAdapter()

    }

    override fun onDetach() {
        super.onDetach()
        binding.containerZoomLayout.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupAdapter() {
//        val imageUrl = chatViewModel.interactingAccount.value?.imageUrl
//        Log.d("ChatFragment", "imageUrl: $imageUrl")
//        chatAdapter = ChatAdapter(requireContext(), imageUrl) {}
//        binding.recyclerMessage.adapter = chatAdapter

        val longListener = object : MessageAdapter.OnItemLongClickListener {
            override fun onDeleteMessage(account: Message) {
                TODO("Not yet implemented")
            }
        }
        val listener = object : MessageAdapter.OnItemClickListener {
            override fun onZoomImage(imageUrl: String) {
                binding.containerZoomLayout.visibility = View.VISIBLE
                Glide.with(binding.imageZoom)
                    .load(imageUrl)
                    .error(R.drawable.ic_loading)
                    .into(binding.imageZoom)
            }
        }
        val adapter = MessageAdapter(avtUrl, longListener, listener)
        adapter.clear()
        adapter.setCurrentAccountEmail(senderEmail ?: "")
        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true

        binding.recyclerMessage.layoutManager = layoutManager
        binding.recyclerMessage.adapter = adapter

        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            adapter.notifyDataSetChanged()
            binding.recyclerMessage.smoothScrollToPosition(messages.size - 1)
            progressBar.visibility = View.GONE
        }
    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as ChitChatterApplication).repository
        chatViewModel = ViewModelProvider(
            requireActivity(), ChatViewModelFactory(repository, senderEmail, receiverEmail)
        )[ChatViewModel::class.java]


        Log.d(
            "ChatFragment",
            "load message with senderEmail: $senderEmail, receiverEmail: $receiverEmail"
        )

        chatViewModel.loadMessage(senderEmail ?: "", receiverEmail ?: "")
        chatViewModel.interactingAccount.observe(viewLifecycleOwner) { account ->
            requireActivity().title = account?.name
            binding.textViewName.text = account?.name
            Log.d("ChatFragment", "Displayname: ${account?.name}")
        }
//        chatViewModel.messages.observe(viewLifecycleOwner) {
//            Log.d("ChatFragment", "Received ${it.size} messages from API")
////            chatAdapter.submitList(it)
//            binding.recyclerMessage.scrollToPosition(it.size - 1)
//            progressBar.visibility = View.GONE
//        }

        chatViewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.imagePhoto.visibility = View.GONE
            } else {
                binding.imagePhoto.visibility = View.VISIBLE
                binding.let { it1 ->
                    Glide.with(it1.imagePhoto).load(it).into(binding.imagePhoto)
                }
            }
        }
    }

    private fun setupViews() {
        binding.textViewName.text = displayName
        if (avtUrl.isNullOrEmpty()) {
            Glide.with(binding.imageViewProfile).load(R.drawable.chitchatter)
                .error(R.drawable.chitchatter).into(binding.imageViewProfile)
        } else {
            try {
                val bucketUrl = "gs://chitchatter-b97bf.appspot.com/avatars/"

                val storage: FirebaseStorage = FirebaseStorage.getInstance()
                val storageRef: StorageReference = storage.getReferenceFromUrl(bucketUrl)
                val imageRef: StorageReference = storageRef.child(avtUrl!!)
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(binding.imageViewProfile).load(uri).error(R.drawable.chitchatter)
                        .into(binding.imageViewProfile)
                }.addOnFailureListener { exception ->
                    // Xử lý lỗi nếu có
                    Log.e("FirebaseStorage", "Failed to get download URL", exception)
                }
            } catch (e: Exception) {
                Glide.with(binding.imageViewProfile).load(R.drawable.chitchatter)
                    .error(R.drawable.chitchatter).into(binding.imageViewProfile)
            }
        }

        binding.btnClose.setOnClickListener {
            binding.containerZoomLayout.visibility = View.GONE
        }
    }

    private fun setupActions() {
        requireActivity().onBackPressedDispatcher.addCallback {
            if (binding.containerZoomLayout.visibility == View.GONE) {
                showNavigationView()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                binding.containerZoomLayout.visibility = View.VISIBLE
            }
        }

        binding.imageViewCall.setOnClickListener {
            voiceCall()
        }
        binding.chatEditInput?.setOnImageAddedListener { contentUri, mimeType, label ->
            chatViewModel.setPhoto(contentUri, mimeType)
            if (binding.chatEditInput?.text.isNullOrBlank()) {
                binding?.chatEditInput?.setText(label)
            }
        }
        binding?.imageViewBack?.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            showNavigationView()
        }
        binding?.btnSend?.setOnClickListener {
            send(senderEmail ?: "", receiverEmail ?: "", token ?: "")
            binding?.imagePhoto?.visibility = View.GONE
            binding?.btnCancel?.visibility = View.GONE
        }
        binding?.chatEditInput?.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                v.requestFocus()
                Log.d("ChatFragment", "Focus on touch")
                binding?.btnRight?.visibility = View.VISIBLE
                binding?.btnCamera?.visibility = View.GONE
                binding?.btnGallery?.visibility = View.GONE
                binding?.btnRecord?.visibility = View.GONE
                v.performClick()
            }
            false
        }
        binding?.btnRight?.setOnClickListener {
            binding?.btnRight?.visibility = View.GONE
            binding?.btnCamera?.visibility = View.VISIBLE
            binding?.btnGallery?.visibility = View.VISIBLE
            binding?.btnRecord?.visibility = View.VISIBLE
        }

        binding?.chatEditInput?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send(senderEmail ?: "", receiverEmail ?: "", token ?: "")
                true
            } else {
                false
            }
        }

        binding?.chatEditInput?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && !v.hasFocus()) {
                binding?.chatEditInput?.clearFocus()
                ChitChatterUtils.hideKeyBoard(requireView())
                binding?.btnRight?.visibility = View.VISIBLE
                binding?.btnCamera?.visibility = View.GONE
                binding?.btnGallery?.visibility = View.GONE
                binding?.btnRecord?.visibility = View.GONE
            } else {
                binding?.btnRight?.visibility = View.GONE
                binding?.btnCamera?.visibility = View.VISIBLE
                binding?.btnGallery?.visibility = View.VISIBLE
                binding?.btnRecord?.visibility = View.VISIBLE
            }
        }
//        if (ContextCompat.checkSelfPermission(
//                requireContext(), Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION
//            )
//        }
        binding.btnCamera.setOnClickListener {
            askPermission(Manifest.permission.CAMERA)
//            if (ContextCompat.checkSelfPermission(
//                    requireContext(), Manifest.permission.CAMERA
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                Log.d("ChatFragment", "Open camera")
//                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                photoUri = createImageUri()
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
//                startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE)
//            } else {
//                Log.d("ChatFragment", "Request camera permission")
//                ActivityCompat.requestPermissions(
//                    requireActivity(),
//                    arrayOf(Manifest.permission.CAMERA),
//                    REQUEST_CAMERA_PERMISSION
//                )
//            }
        }
//        if (ContextCompat.checkSelfPermission(
//                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                REQUEST_GALLERY_PERMISSION
//            )
//        }
        binding.btnGallery?.setOnClickListener {
            askPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
//            if (ContextCompat.checkSelfPermission(
//                    requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                Log.d("ChatFragment", "Open gallery")
//                val intent =
//                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                intent.type = "image/*"
//                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
//            } else {
//                Log.d("ChatFragment", "Request gallery permission")
//                ActivityCompat.requestPermissions(
//                    requireActivity(),
//                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                    REQUEST_GALLERY_PERMISSION
//                )
//            }
        }
        binding?.btnRecord?.setOnClickListener {
            Log.d("ChatFragment", "Record voice")
        }
        binding?.btnCancel?.setOnClickListener {
            binding?.imagePhoto?.setImageDrawable(null)
            binding?.imagePhoto?.visibility = View.GONE
            binding?.btnCancel?.visibility = View.GONE
        }

    }

    private fun createImageUri(): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        return requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )!!
    }


    private fun send(senderEmail: String, receiverEmail: String, token: String) {
        val text = binding?.chatEditInput?.text.toString() ?: ""
//        val imageUri = chatViewModel.photo.value
        Log.d("ChatFragment", "text: $text, photo: $photoUri")
        if (text.isNotEmpty() || photoUri != null) {
            chatViewModel.sendMessage(text, senderEmail, receiverEmail, token)
            binding?.chatEditInput?.text?.clear()
        } else {
            Log.d("ChatFragment", "Empty message")
        }
//        binding?.chatEditInput?.text?.let { text ->
//            if (text.isNotEmpty()) {
//                chatViewModel.sendMessage(text.toString(), senderEmail, receiverEmail, token)
//                text.clear()
//            } else {
//                Log.d("ChatFragment", "Empty message")
//
//            }
//        }
    }

    fun selectImageAndSendMessage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun voiceCall() {

    }

    override fun onStart() {
        super.onStart()
        // active => update tin nhan
        ChatViewModel.isActive = true
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        progressBar.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        // inactive => ko update tin nhan
        ChatViewModel.isActive = false
        progressBar.visibility = View.GONE
    }

    private fun hideNavigationView() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.hideNavigation()
    }

    private fun showNavigationView() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.showNavigation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    val selectedImageUri = data.data
                    binding?.imagePhoto?.setImageURI(selectedImageUri)
                    binding?.imagePhoto?.visibility = View.VISIBLE
                    binding?.btnCancel?.visibility = View.VISIBLE

                    val imageUri = data.data
                    photoUri = imageUri
                    Log.d("ChatFragment", "Gallery imageUri: $imageUri")
                    if (imageUri != null) {
                        val receiverEmail = receiverEmail ?: ""
                        val senderEmail = senderEmail ?: ""
                        val token = token ?: ""
                        chatViewModel.uploadImageAndSendMessage(
                            imageUri, receiverEmail, senderEmail, token
                        )


                    }
                }

                REQUEST_CODE_CAPTURE_IMAGE -> {
                    val imageBitmap = data.extras?.get("data") as Bitmap
                    binding?.imagePhoto?.setImageBitmap(imageBitmap)
                    binding?.imagePhoto?.visibility = View.VISIBLE
                    binding?.btnCancel?.visibility = View.VISIBLE

                    val imageUri = getImageUriFromBitmap(imageBitmap)
                    photoUri = imageUri
                    Log.d("ChatFragment", "Camera imageUri: $imageUri")
                    if (imageUri != null) {
                        val receiverEmail = receiverEmail ?: ""
                        val senderEmail = senderEmail ?: ""
                        val token = token ?: ""
                        chatViewModel.uploadImageAndSendMessage(
                            imageUri, receiverEmail, senderEmail, token
                        )
                    }
                }
            }
        }
//        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
//            val imageUri = data.data
//            Log.d("ChatFragment", "imageUri: $imageUri")
//            if (imageUri != null) {
//                val receiverEmail = receiverEmail ?: ""
//                val senderEmail = senderEmail ?: ""
//                val token = token ?: ""
//                chatViewModel.uploadImageAndSendMessage(imageUri, receiverEmail, senderEmail, token)
//            }
//        }
    }

    fun getImageUriFromBitmap(bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    fun askPermission(permission: String) {
        // Kiểm tra quyền truy cập
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(), permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                if (permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    pickImageFromGallery()
                } else if (permission == Manifest.permission.CAMERA) {
                    pickImageFromCamera()
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), permission
            ) -> {
                if (permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    showMessage(R.string.gallery_permission_prompt, Snackbar.LENGTH_LONG, true)
                } else if (permission == Manifest.permission.CAMERA) {
                    showMessage(R.string.camera_permission_prompt, Snackbar.LENGTH_LONG, true)
                }
            }

            else -> {
                if (permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else if (permission == Manifest.permission.CAMERA) {
                    requestCameraPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun pickImageFromCamera() {
        // Intent để chọn ảnh từ gallery\
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE)
    }

    private fun pickImageFromGallery() {
        // Intent để chọn ảnh từ gallery\
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun showMessage(messageId: Int, duration: Int, showAction: Boolean = false) {
        val snackBar = Snackbar.make(binding.root, messageId, duration)
        if (showAction && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            snackBar.setAction("OK") {
                requestGalleryPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            snackBar.setAction("No thank") {
                showMessage(R.string.camera_permission_denied, Snackbar.LENGTH_LONG)
            }
        }
        snackBar.show()
    }

    companion object {
        //        private const val ARG_ACCOUNT = "account"
        private const val ARG_EMAIL = "email"
        private const val ARG_SENDER_EMAIL = "sender_email"
        private const val ARG_RECEIVER_EMAIL = "receiver_email"
        private const val ARG_DISPLAY_NAME = "display_name"
        private const val ARG_AVT_URL = "avt_url"
        private const val ARG_MESSAGE_JSON = "message_json"
        private const val ARG_TOKEN = "token"


        @JvmStatic
        fun newInstance(email: String) = ChatFragment().apply {
            arguments = Bundle().apply {
                putString("email", email)
                Log.d("email", email)
            }
        }

        //        fun newInstance(senderEmail: String, receiverEmail: String, displayName: String) =
//            ChatFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_SENDER_EMAIL, senderEmail)
//                    putString(ARG_RECEIVER_EMAIL, receiverEmail)
//                    putString(ARG_DISPLAY_NAME, displayName)
//
//                }
//            }
        fun newInstance(
            senderEmail: String,
            receiverEmail: String,
            displayName: String,
            imageUrl: String?,
            messageJson: String?,
            token: String
        ) = ChatFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SENDER_EMAIL, senderEmail)
                putString(ARG_RECEIVER_EMAIL, receiverEmail)
                putString(ARG_DISPLAY_NAME, displayName)
                putString(ARG_AVT_URL, imageUrl)
                putString(ARG_MESSAGE_JSON, messageJson)
                putString(ARG_TOKEN, token)
            }
        }

    }
}