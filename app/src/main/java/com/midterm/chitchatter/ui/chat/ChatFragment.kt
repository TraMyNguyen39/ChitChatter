package com.midterm.chitchatter.ui.chat

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.midterm.chitchatter.databinding.FragmentChatBinding
import com.midterm.chitchatter.service.MessengerApplication


class ChatFragment : Fragment() {

    private var binding: FragmentChatBinding? = null
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding?.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)
        setupViewModel()
        setupViews()
        setupActions()
    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as MessengerApplication).repository
        chatViewModel = ViewModelProvider(
            requireActivity(), ChatViewModelFactory(repository)
        )[ChatViewModel::class.java]

        chatViewModel.loadMessage()
        chatViewModel.interactingAccount.observe(viewLifecycleOwner) {
            requireActivity().title = it?.displayName
            // todo
        }
        chatViewModel.messages.observe(viewLifecycleOwner) {
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

//        MessengerService.remoteMessage.observe(requireActivity()) {
//            if (ChatViewModel.isActive) {
//                chatViewModel.pushIncomingMessage(it.data)
//            }
//        }
    }

    private fun setupViews() {
//        navBar = requireActivity().findViewById(R.id.bottom_nav)
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
//        navBar.visibility = View.VISIBLE
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
}