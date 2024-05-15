package com.midterm.chitchatter.ui.contacts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.midterm.chitchatter.R
import com.midterm.chitchatter.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {
    private lateinit var binding: FragmentContactsBinding
    private lateinit var tvTitle: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        tvTitle = requireActivity().findViewById(R.id.tv_title)
//        tvTitle.text = getString(R.string.title_contacts)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }
}