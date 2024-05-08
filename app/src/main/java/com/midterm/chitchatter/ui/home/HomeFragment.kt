package com.midterm.chitchatter.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.databinding.FragmentHomeBinding
import com.midterm.chitchatter.ui.chat.ChatFragment
import com.midterm.chitchatter.utils.ChitChatterUtils

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: AccountAdapter
    private lateinit var tvTitle: TextView

    private lateinit var viewModel: HomeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()

//        tvTitle = requireActivity().findViewById(R.id.tv_title)
//        tvTitle.text = getString(R.string.title_message)

        adapter = AccountAdapter(object : AccountAdapter.OnItemClickListener {
            override fun onItemClick(account: Account) {
                Log.e("HomeFragment", "Item clicked: ${account.email}")
                navigateToChatFragment(account)
            }
        })

        binding.rvHome.adapter = adapter

        val currentAccount = ChitChatterUtils.getCurrentAccount(requireContext())
        if (currentAccount != null) {
            viewModel.setCurrentAccount(currentAccount)
        };

        if (currentAccount != null) {
            viewModel.fetchAllLastMessages(currentAccount)
        }

        viewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            messages?.let {
                adapter.updateMessage(it.filterNotNull())
            }
        })

    }

    private fun setUpViewModel() {
        val repository = (requireActivity().application as ChitChatterApplication).repository

        viewModel = ViewModelProvider(
            requireActivity(),
            HomeViewModelFactory(repository)
        )[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun navigateToChatFragment(account: Account) {
        val chatFragment = ChatFragment.newInstance(account)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.include_main, chatFragment)
            .addToBackStack(null)
            .commit()
    }

}