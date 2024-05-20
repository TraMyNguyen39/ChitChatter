package com.midterm.chitchatter.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.databinding.FragmentContactsBinding
import com.midterm.chitchatter.utils.ChitChatterUtils

class ContactFragment : Fragment() {
    private lateinit var binding: FragmentContactsBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: ContactViewModel
    private lateinit var adapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        progressBar = requireActivity().findViewById(R.id.progress_bar_main)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        setupAdapter()
        setUpActions()

        progressBar.visibility = View.VISIBLE
        viewModel.loadAllContact(ChitChatterUtils.getCurrentAccount(requireContext()))
    }

    private fun setUpViewModel() {
        val repository = (requireActivity().application as ChitChatterApplication).repository

        viewModel = ViewModelProvider(
            requireActivity(),
            ContactViewModelFactory(repository)
        )[ContactViewModel::class.java]

        viewModel.contacts.observe(viewLifecycleOwner) {
            adapter.updateData(it)
            progressBar.visibility = View.GONE
//            adapter.findHeaderPositions()
        }


        viewModel.searchResults.observe(viewLifecycleOwner) {
            adapter.updateData(it)
            progressBar.visibility = View.GONE
//            adapter.findHeaderPositions()
        }
    }

    private fun setupAdapter() {
        val listenerDetail = object : ContactAdapter.OnItemClickListener {
            override fun onItemClick(account: Account) {
                moveToContactDetail(account.email)
            }
        }

        val listenerChat = object : ContactAdapter.OnItemClickListener {
            override fun onItemClick(account: Account) {
//                moveToContactDetail(account.email)
            }
        }
        adapter = ContactAdapter(
            mutableListOf(), listenerDetail, listenerChat
        )

        binding.rvContacts.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        binding.rvContacts.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        binding.rvContacts.layoutManager = linearLayoutManager
    }

    private fun setUpActions() {
        binding.searchContacts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                progressBar.visibility = View.VISIBLE
                viewModel.searchDebounced(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                progressBar.visibility = View.VISIBLE
                viewModel.searchDebounced(newText)
                return false
            }
        })
    }


    private fun moveToContactDetail(email: String) {
//        val email = ChitChatterUtils.getCurrentAccount(requireContext())
        val action = ContactFragmentDirections.actionContactsFragmentToAccountFragment(email)
        findNavController().navigate(action)
    }
}