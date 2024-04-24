package com.midterm.chitchatter.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.model.Notification
import com.midterm.chitchatter.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: AccountAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.title_message)
        adapter = AccountAdapter(mutableListOf(), object : AccountAdapter.OnItemClickListener {
            override fun onItemClick(account: Account) {

            }
        })
        binding.rvHome.adapter = adapter

        HomeViewModel.currentAccount.observe(viewLifecycleOwner, Observer {

        })

        HomeViewModel.contacts.observe(viewLifecycleOwner, Observer { contacts ->
            contacts?.let {
                adapter.updateAccounts(it.filterNotNull())
            }
        })

        HomeViewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            messages?.let {
                adapter.updateMessage(it.filterNotNull().toSet())
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

}