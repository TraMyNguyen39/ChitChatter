package com.midterm.chitchatter.ui.message

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.midterm.chitchatter.R
import com.midterm.chitchatter.adapter.MessageAdapter
import com.midterm.chitchatter.data.model.Data
import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.model.MessageStatus
import com.midterm.chitchatter.data.model.Notification
import com.midterm.chitchatter.databinding.FragmentMessageBinding



class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMessageBinding.bind(view)

        // Create dummy data
        val messages = listOf(
            Message(1, "your_username", "receiver", Data("Hello!"), Notification("New message"), status = MessageStatus.SENT),
            Message(2, "receiver", "your_username", Data("Hi!"), Notification("New message"), status = MessageStatus.RECEIVED),
            // Add more messages...
        )

        // Initialize RecyclerView
        val adapter = MessageAdapter(messages)
        _binding?.recyclerViewMessage?.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MessageFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}