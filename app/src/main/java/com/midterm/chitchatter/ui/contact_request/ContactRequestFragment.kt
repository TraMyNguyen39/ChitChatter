package com.midterm.chitchatter.ui.contact_request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.data.model.ContactRequestSender
import com.midterm.chitchatter.databinding.FragmentContactRequestBinding
import com.midterm.chitchatter.ui.MainActivity
import com.midterm.chitchatter.ui.contacts.ContactFragmentDirections
import com.midterm.chitchatter.utils.ChitChatterUtils

class ContactRequestFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentContactRequestBinding
    private lateinit var adapter: ContactRequestAdapter
    private lateinit var viewModel: ContactRequestViewModel
    private lateinit var progressBar: ProgressBar
    private var userEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userEmail = ChitChatterUtils.getCurrentAccount(requireContext())
        if (userEmail == null) {
            requireActivity().finish()
        }
        binding = FragmentContactRequestBinding.inflate(inflater, container, false)
        progressBar = requireActivity().findViewById(R.id.progress_bar_main)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        setupAdapter()
        setUpActions()

        viewModel.markAllAsRead(userEmail!!)
//        viewModel.loadAllRequests(userEmail)
        setupRealTimeNotification()
    }

    override fun onStart() {
        super.onStart()
        progressBar.visibility = View.VISIBLE
        binding.rvContactRequests.visibility = View.INVISIBLE
    }

    private fun setUpViewModel() {
        val repository = (requireActivity().application as ChitChatterApplication).repository

        viewModel = ViewModelProvider(
            requireActivity(),
            ContactRequestViewModelFactory(repository)
        )[ContactRequestViewModel::class.java]

        viewModel.contactRequests.observe(viewLifecycleOwner) { list ->
            progressBar.visibility = View.GONE
            if (list.isNotEmpty()) {
                adapter.updateData(list)
                binding.rvContactRequests.visibility = View.VISIBLE
                binding.emptyView.root.visibility = View.GONE
            } else {
                binding.emptyView.root.visibility = View.VISIBLE
                binding.rvContactRequests.visibility = View.GONE
            }
        }
    }

    private fun setupAdapter() {
        val listener = object : ContactRequestAdapter.OnItemClickListener {
            override fun onDetailItemClick(requestSender: ContactRequestSender) {
                moveToContactDetail(requestSender.email)
            }

            override fun onAcceptItemClick(requestSender: ContactRequestSender) {
                progressBar.visibility = View.VISIBLE
                binding.rvContactRequests.visibility = View.GONE

                viewModel.acceptContact(userEmail!!, requestSender.email) { isSuccessful ->
                    if (isSuccessful) {
                        Snackbar.make(
                            requireView(),
                            "Bạn đã kết nối với ${requestSender.email}",
                            Snackbar.LENGTH_LONG
                        ).show()
                        viewModel.deleteRequest(requestSender)
                    } else {
                        Snackbar.make(
                            requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                        ).show()
                    }
                    progressBar.visibility = View.GONE
                    binding.rvContactRequests.visibility = View.VISIBLE
                }
            }

            override fun onRejectItemClick(requestSender: ContactRequestSender) {
                progressBar.visibility = View.VISIBLE
                binding.rvContactRequests.visibility = View.GONE
                viewModel.rejectContact(userEmail!!, requestSender.email) { isSuccessful ->
                    if (isSuccessful) {
                        viewModel.deleteRequest(requestSender)
                    } else {
                        Snackbar.make(
                            requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG
                        ).show()
                    }
                    progressBar.visibility = View.GONE
                    binding.rvContactRequests.visibility = View.VISIBLE
                }
            }
        }

        adapter = ContactRequestAdapter(
            mutableListOf(), listener
        )

        binding.rvContactRequests.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        val dividerItemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.rvContactRequests.adapter = adapter
        binding.rvContactRequests.addItemDecoration(dividerItemDecoration)
    }

    private fun setUpActions() {
       binding.refreshRequests.setOnRefreshListener(this)
    }

    private fun setupRealTimeNotification() {
        // Set up Firebase Realtime Database listener
        val database = FirebaseDatabase.getInstance()
        val splitEmail = userEmail!!.split("@")[0]
        val notificationsRef = database.getReference("requestContact/${splitEmail}")

        notificationsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                viewModel.loadAllRequests(userEmail!!)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                viewModel.loadAllRequests(userEmail!!)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                viewModel.loadAllRequests(userEmail!!)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Notification moved
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun moveToContactDetail(email: String) {
        val action =
            ContactFragmentDirections.actionContactsFragmentToAccountFragment(email)
        findNavController().popBackStack()
        findNavController().navigate(action)
    }

    override fun onRefresh() {
        progressBar.visibility = View.VISIBLE
        binding.rvContactRequests.visibility = View.GONE
        binding.refreshRequests.isRefreshing = false
        viewModel.loadAllRequests(userEmail)
    }
}