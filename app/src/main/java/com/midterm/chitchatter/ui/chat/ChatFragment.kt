package com.midterm.chitchatter.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.ChitChatterService
import com.midterm.chitchatter.adapter.MessageAdapter
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.model.MessageStatus
import com.midterm.chitchatter.data.model.Notification
import com.midterm.chitchatter.databinding.FragmentChatBinding
import com.midterm.chitchatter.ui.MainActivity


class ChatFragment : Fragment() {

    private var binding: FragmentChatBinding? = null
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    private var email: String? = null
    private var senderEmail: String? = null
    private var receiverEmail: String? = null
    private var displayName: String? = null
    private lateinit var viewModelFactory: ChatViewModelFactory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString(ARG_EMAIL)
        senderEmail = arguments?.getString(ARG_SENDER_EMAIL)
        receiverEmail = arguments?.getString(ARG_RECEIVER_EMAIL)
        displayName = arguments?.getString(ARG_DISPLAY_NAME)
        val repository = (requireActivity().application as ChitChatterApplication).repository
        viewModelFactory = ChatViewModelFactory(repository)
        chatViewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]
        chatViewModel.updateInteractingAccount(Account(email = receiverEmail?: ""))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
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
        adapter.setCurrentAccountEmail(senderEmail ?: "") // Set currentAccountEmail here
        binding?.recyclerMessage?.layoutManager = LinearLayoutManager(context)
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
            requireActivity(), ChatViewModelFactory(repository)
        )[ChatViewModel::class.java]

        chatViewModel.loadMessage(senderEmail ?: "", receiverEmail ?: "")
        chatViewModel.interactingAccount.observe(viewLifecycleOwner) { account ->
            requireActivity().title = account?.name
            binding?.textViewName?.text = account?.name // Set the display name here
            Log.d("ChatFragment", "Displayname: ${account?.name}")
        }
        chatViewModel.messages.observe(viewLifecycleOwner) {
            Log.d("ChatFragment", "Received ${it.size} messages from API")
            chatAdapter.submitList(it)
            binding?.recyclerMessage?.scrollToPosition(it.size - 1)
        }

        chatViewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding?.imagePhoto?.visibility = View.GONE
            } else {
                binding?.imagePhoto?.visibility = View.VISIBLE
                binding?.let { it1 -> Glide.with(it1.imagePhoto).load(it).into(binding!!.imagePhoto) }
            }
        }

        ChitChatterService.remoteMessage.observe(requireActivity()) {
            if (ChatViewModel.isActive) {
                chatViewModel.pushIncomingMessage(it.data)
            }
        }
    }

    private fun setupViews() {

        val imageUrl = chatViewModel.interactingAccount.value?.imageUrl
        chatAdapter = ChatAdapter(requireContext(), imageUrl) {}
        binding?.recyclerMessage?.adapter = chatAdapter
    }

    private fun setupActions() {
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
            send()
        }
        binding?.imageAttach?.setOnClickListener {
//            attach()
        }
        binding?.chatEditInput?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send()
                true
            } else {
                false
            }
        }

        binding?.chatEditInput?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && !v.hasFocus()) {
                binding?.chatEditInput?.clearFocus()
                closeKeyboard()
            }
//            else {
//                if (hasFocus) {
////                    navBar.visibility = View.GONE
//
//                }
//            }
        }
    }

    private fun closeKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding?.root?.windowToken, 0)
    }

    private fun send() {
        binding?.chatEditInput?.text?.let { text ->
            if (text.isNotEmpty()) {
                chatViewModel.sendMessage(text.toString())
                text.clear()
            }
            else{
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

    }
}