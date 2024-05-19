package com.midterm.chitchatter.ui.contacts

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.data.model.Account
import com.midterm.chitchatter.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {
    private lateinit var binding: FragmentContactsBinding
    private lateinit var viewModel: ContactsViewModel
    private lateinit var adapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        setupAdapter()
    }

    private fun setUpViewModel() {
        val repository = (requireActivity().application as ChitChatterApplication).repository

        viewModel = ViewModelProvider(
            requireActivity(),
            ContactsViewModelFactory(repository)
        )[ContactsViewModel::class.java]

        viewModel.contacts.observe(viewLifecycleOwner) {
            adapter.updateData(it)
//            adapter.findHeaderPositions()
        }

        viewModel.contactDetail.observe(viewLifecycleOwner) {
            moveToContactDetail()
        }
    }

    private fun setupAdapter() {
        adapter = ContactAdapter(
            mutableListOf(), object : ContactAdapter.OnItemClickListener {
                override fun onItemClick(account: Account) {
                    viewModel.selectContactDetail(account)
                }

            }
        )

        binding.rvContacts.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false)

        binding.rvContacts.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        binding.rvContacts.layoutManager = linearLayoutManager
    }

    private fun moveToContactDetail() {
    }
}