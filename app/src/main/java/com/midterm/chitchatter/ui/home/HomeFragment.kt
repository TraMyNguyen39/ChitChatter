package com.midterm.chitchatter.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.databinding.FragmentHomeBinding
import com.midterm.chitchatter.ui.chat.ChatFragment
import com.midterm.chitchatter.utils.ChitChatterUtils

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: AccountAdapter
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: HomeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = requireActivity().findViewById(R.id.progress_bar_main)

        setUpViewModel()

        adapter = AccountAdapter(object : AccountAdapter.OnItemClickListener {
            override fun onItemClick(message: Message) {
                val senderEmail = ChitChatterUtils.getCurrentAccount(requireContext()) ?: ""
                val receiverEmail =  if (message.isIncoming) message.sender else message.receiver
                val displayName = if (message.isIncoming) message.name else message.name
                Log.d("HomeFragment", "senderEmail: $senderEmail, receiverEmail: $receiverEmail")
                navigateToChatFragment(senderEmail , receiverEmail,displayName)

            }
        })

        binding.rvHome.adapter = adapter

        val currentAccount = ChitChatterUtils.getCurrentAccount(requireContext())
        if (currentAccount != null) {
            viewModel.setCurrentAccount(currentAccount)
        };

        if (currentAccount != null) {
            progressBar.visibility = View.VISIBLE
            viewModel.fetchAllLastMessages(currentAccount)
        }

        viewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            messages?.let {
                adapter.updateMessage(it.filterNotNull())
            }
            progressBar.visibility = View.GONE
        })

    }

    private fun setUpViewModel() {
        val repository = (requireActivity().application as ChitChatterApplication).repository

        viewModel = ViewModelProvider(
            requireActivity(),
            HomeViewModelFactory(repository)
        )[HomeViewModel::class.java]
    }

    override fun onStop() {
        super.onStop()
        progressBar.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun navigateToChatFragment(senderEmail: String, receiverEmail: String, displayName: String) {
        val chatFragment = ChatFragment.newInstance(senderEmail, receiverEmail,displayName)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(this@HomeFragment.id, chatFragment)
            .addToBackStack(null)
            .commit()
    }
}