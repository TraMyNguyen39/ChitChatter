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
import com.google.android.material.snackbar.Snackbar
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.databinding.FragmentContactsBinding
import com.midterm.chitchatter.ui.chat.ChatFragment
import com.midterm.chitchatter.utils.ChitChatterUtils
import com.midterm.chitchatter.utils.ContactStatus

class ContactFragment : Fragment() {
    private lateinit var binding: FragmentContactsBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: ContactViewModel
    private lateinit var adapter: ContactAdapter
    private var userEmail : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userEmail = ChitChatterUtils.getCurrentAccount(requireContext())!!
        if (userEmail == null) {
            requireActivity().finish()
        }
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
        }


        viewModel.searchResults.observe(viewLifecycleOwner) {
            adapter.updateData(it)
            progressBar.visibility = View.GONE
        }
    }

    private fun setupAdapter() {

        val listener = object : ContactAdapter.OnItemClickListener {
            override fun onDetailItemClick(account: Account) {
                moveToContactDetail(account.email, account.contactStatus)
            }

            override fun onChatItemClick(account: Account) {
                moveToChatFragment(account.email, account.name)
            }

            override fun onAddItemClick(account: Account) {
                val userEmail = ChitChatterUtils.getCurrentAccount(requireContext())
                if (userEmail != null) {
                    viewModel.addContact(userEmail, account.email) { isSuccessful ->
                        if (isSuccessful) {
                            Snackbar.make(
                                requireView(), "Đã gửi lời mời kết bạn!", Snackbar.LENGTH_LONG
                            ).show()
                            account.contactStatus = ContactStatus.REQUESTED.ordinal
                        } else {
                            Snackbar.make(
                                requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

            override fun onAcceptItemClick(account: Account) {
                TODO("Not yet implemented")
            }

            override fun onRejectItemClick(account: Account) {
                val userEmail = ChitChatterUtils.getCurrentAccount(requireContext())
                if (userEmail != null) {
                    viewModel.removeContact(userEmail, account.email) { isSuccessful ->
                        if (isSuccessful) {
                            account.contactStatus = ContactStatus.UNCONNECTED.ordinal
                        } else {
                            Snackbar.make(
                                requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        adapter = ContactAdapter(
            mutableListOf(), listener
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
        val searchView: SearchView = binding.searchContacts
        searchView.isIconifiedByDefault = false

        binding.searchContacts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                progressBar.visibility = View.VISIBLE
                viewModel.searchDebounced(query, userEmail!!)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                progressBar.visibility = View.VISIBLE
                viewModel.searchDebounced(newText, userEmail!!)
                return false
            }
        })
    }

    private fun moveToContactDetail(email: String, contactStatus: Int) {
//        val email = ChitChatterUtils.getCurrentAccount(requireContext())
        val action = ContactFragmentDirections.actionContactsFragmentToAccountFragment(email, contactStatus)
        findNavController().navigate(action)
    }

    private fun moveToChatFragment(receiverEmail: String, displayName: String) {
        val chatFragment = ChatFragment.newInstance(userEmail!!, receiverEmail, displayName)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(this@ContactFragment.id, chatFragment)
            .addToBackStack(null)
            .commit()
    }
}