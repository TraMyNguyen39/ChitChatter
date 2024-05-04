package com.midterm.chitchatter.ui.forget_password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.databinding.FragmentForgetPasswordBinding
import com.midterm.chitchatter.utils.ChitChatterUtils
import com.midterm.chitchatter.utils.ChitChatterUtils.afterTextChanged
import com.midterm.chitchatter.utils.ChitChatterUtils.isOnline

class ForgetPasswordFragment : Fragment() {
    private lateinit var binding: FragmentForgetPasswordBinding
    private lateinit var viewModel: ForgetPasswordViewModel
    private var isCorrectForm = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setUpViewModel()
    }

    override fun onPause() {
        super.onPause()
        removeForm()
    }
    private fun setUpViewModel() {
        val repository = (requireActivity().application as ChitChatterApplication).repository

        viewModel = ViewModelProvider(
            requireActivity(),
            ForgetPasswordViewModelFactory(repository)
        )[ForgetPasswordViewModel::class.java]

        viewModel.forgetPassFormState.observe(viewLifecycleOwner) {
            if (it != null)
                binding.editForgetPassEmail.error = getString(it)
            isCorrectForm = it == null
        }

        viewModel.forgetPassState.observe(viewLifecycleOwner) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupListener() {
        binding.editForgetPassEmail.afterTextChanged {
            viewModel.forgetPasswordFormChanged(it)
        }

        binding.btnForgetPassBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnForgetPassConfirmEmail.setOnClickListener {
            ChitChatterUtils.hideKeyBoard(requireView())
            if (isOnline(requireContext())) {
                if (isCorrectForm) {
                    val email = binding.editForgetPassEmail.text.toString()
                    viewModel.resetPassword(email)
                } else {
                    Snackbar.make(
                        binding.root,
                        R.string.error_email,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.message_no_internet,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun removeForm() {
        binding.editForgetPassEmail.setText("")
        isCorrectForm = false
    }
}