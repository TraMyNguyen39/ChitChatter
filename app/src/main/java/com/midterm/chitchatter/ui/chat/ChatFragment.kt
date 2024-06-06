package com.midterm.chitchatter.ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.ChitChatterService
import com.midterm.chitchatter.R
import com.midterm.chitchatter.adapter.MessageAdapter
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.databinding.FragmentChatBinding
import com.midterm.chitchatter.ui.MainActivity
import com.midterm.chitchatter.utils.ChitChatterUtils

class ChatFragment : Fragment() {

    private var binding: FragmentChatBinding? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private var email: String? = null
    private var senderEmail: String? = null
    private var receiverEmail: String? = null
    private var displayName: String? = null
    private var token: String? = null
    private lateinit var viewModelFactory: ChatViewModelFactory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val messageJson = arguments?.getString(ARG_MESSAGE_JSON)
        val gson = Gson()
        val message = gson.fromJson(messageJson, Message::class.java)
        token = arguments?.getString(ARG_TOKEN)
        Log.d("ChatFragment", "token: $token")
        email = arguments?.getString(ARG_EMAIL)
        senderEmail = arguments?.getString(ARG_SENDER_EMAIL)
        receiverEmail = arguments?.getString(ARG_RECEIVER_EMAIL)
        displayName = arguments?.getString(ARG_DISPLAY_NAME)
        val repository = (requireActivity().application as ChitChatterApplication).repository


        viewModelFactory = ChatViewModelFactory(repository, senderEmail, receiverEmail)

        chatViewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]
        chatViewModel.updateInteractingAccount(Account(email = receiverEmail ?: ""))

//        viewModelFactory = ChatViewModelFactory(repository)
//        chatViewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]
        if (senderEmail != null && receiverEmail != null) {
            chatViewModel = ViewModelProvider(this, ChatViewModelFactory(repository, senderEmail, receiverEmail)).get(ChatViewModel::class.java)
        }
        else
        {
            Log.d("ChatFragment", "senderEmail: $senderEmail, receiverEmail: $receiverEmail")
//            chatViewModel = ViewModelProvider(this, ChatViewModelFactory(repository, email)).get(ChatViewModel::class.java)
        }

        val interactingAccountEmail = if (message.isIncoming) receiverEmail else senderEmail
        Log.d("ChatFragment", "interactingAccountEmail: $interactingAccountEmail")
        chatViewModel.updateInteractingAccount(Account(email = interactingAccountEmail?: ""))
        chatViewModel.updateInteractingAccountToken(Account(email = interactingAccountEmail?: "").token ?: "")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        progressBar = requireActivity().findViewById(R.id.progress_bar_main)
        return binding?.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)
        hideNavigationView()
        setupViewModel()
        setupViews()
        setupActions()
        val adapter = MessageAdapter()
        adapter.clear()
        adapter.setCurrentAccountEmail(senderEmail ?: "")
        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
//        layoutManager.reverseLayout = true

        binding?.recyclerMessage?.layoutManager = layoutManager
        binding?.recyclerMessage?.adapter = adapter
        binding?.textViewName?.text = displayName

        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            adapter.notifyDataSetChanged()
            binding?.recyclerMessage?.scrollToPosition(messages.size - 1)
        }
    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as ChitChatterApplication).repository
        chatViewModel = ViewModelProvider(
            requireActivity(), ChatViewModelFactory(repository, senderEmail, receiverEmail)
        )[ChatViewModel::class.java]

        progressBar.visibility = View.VISIBLE

        Log.d("ChatFragment", "load message with senderEmail: $senderEmail, receiverEmail: $receiverEmail")

        chatViewModel.loadMessage(senderEmail ?: "", receiverEmail ?: "")
        chatViewModel.interactingAccount.observe(viewLifecycleOwner) { account ->
            requireActivity().title = account?.name
            binding?.textViewName?.text = account?.name
            Log.d("ChatFragment", "Displayname: ${account?.name}")
        }
        chatViewModel.messages.observe(viewLifecycleOwner) {
            Log.d("ChatFragment", "Received ${it.size} messages from API")
            chatAdapter.submitList(it)
            binding?.recyclerMessage?.scrollToPosition(it.size - 1)
            progressBar.visibility = View.GONE
        }

        chatViewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding?.imagePhoto?.visibility = View.GONE
            } else {
                binding?.imagePhoto?.visibility = View.VISIBLE
                binding?.let { it1 ->
                    Glide.with(it1.imagePhoto).load(it).into(binding!!.imagePhoto)
                }
            }
        }

//        ChitChatterService.remoteMessage.observe(requireActivity()) {
//            if (ChatViewModel.isActive) {
//                chatViewModel.pushIncomingMessage(it.data)
//            }
//        }
    }

    private fun setupViews() {
        val imageUrl = chatViewModel.interactingAccount.value?.imageUrl
        Log.d("ChatFragment", "imageUrl: $imageUrl")
        chatAdapter = ChatAdapter(requireContext(), imageUrl) {}
        binding?.recyclerMessage?.adapter = chatAdapter
//        Glide.with(binding?.imagePhoto ?: return).load(imageUrl).into(binding?.imageViewProfile ?: return)
    }

    private fun setupActions() {
        requireActivity().onBackPressedDispatcher.addCallback {
            requireActivity().supportFragmentManager.popBackStack()
            showNavigationView()
        }

        binding?.imageViewCall?.setOnClickListener {
            voiceCall()
        }
        binding?.chatEditInput?.setOnImageAddedListener { contentUri, mimeType, label ->
            chatViewModel.setPhoto(contentUri, mimeType)
            if (binding?.chatEditInput?.text.isNullOrBlank()) {
                binding?.chatEditInput?.setText(label)
            }
        }
        binding?.imageViewBack?.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            showNavigationView()
        }
        binding?.imageSend?.setOnClickListener {
            send(senderEmail ?: "", receiverEmail ?: "", token ?: "")
        }
        binding?.imageAttach?.setOnClickListener {
//            attach()
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
            }
//            else {
//                if (hasFocus) {
////                    navBar.visibility = View.GONE
//
//                }
//            }
        }
    }


    private fun send(senderEmail: String, receiverEmail: String, token: String) {
        binding?.chatEditInput?.text?.let { text ->
            if (text.isNotEmpty()) {
                chatViewModel.sendMessage(text.toString(), senderEmail, receiverEmail, token)
                text.clear()
            } else {
                Log.d("ChatFragment", "Empty message")

            }
        }
    }

    private fun voiceCall() {

    }

    override fun onStart() {
        super.onStart()
        // active => update tin nhan
        ChatViewModel.isActive = true
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

    companion object {
        //        private const val ARG_ACCOUNT = "account"
        private const val ARG_EMAIL = "email"
        private const val ARG_SENDER_EMAIL = "sender_email"
        private const val ARG_RECEIVER_EMAIL = "receiver_email"
        private const val ARG_DISPLAY_NAME = "display_name"
        private const val ARG_MESSAGE_JSON = "message_json"
        private const val ARG_TOKEN = "token"


        @JvmStatic
        fun newInstance(email: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString("email", email)
                    Log.d("email", email)
                }
            }

        fun newInstance(senderEmail: String, receiverEmail: String, displayName: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SENDER_EMAIL, senderEmail)
                    putString(ARG_RECEIVER_EMAIL, receiverEmail)
                    putString(ARG_DISPLAY_NAME, displayName)

                }
            }
        fun newInstance(senderEmail: String, receiverEmail: String, displayName: String, messageJson: String, token: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SENDER_EMAIL, senderEmail)
                    putString(ARG_RECEIVER_EMAIL, receiverEmail)
                    putString(ARG_DISPLAY_NAME, displayName)
                    putString(ARG_MESSAGE_JSON, messageJson)
                    putString(ARG_TOKEN, token)
                }
            }

    }
}