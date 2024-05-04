package com.midterm.chitchatter.ui.register

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.databinding.FragmentRegisterBinding
import com.midterm.chitchatter.utils.ChitChatterUtils
import com.midterm.chitchatter.utils.ChitChatterUtils.afterTextChanged
import com.midterm.chitchatter.utils.ChitChatterUtils.isOnline

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private var isCorrectForm = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupViewModel()
    }
    override fun onPause() {
        super.onPause()
        removeForm()
    }

    private fun setupListener() {
        binding.editSignupEmail.afterTextChanged {
            val displayName = binding.editSignupDisplayName.text.toString()
            val password = binding.editSignupPassword.text.toString()
            val confirmPwd = binding.editSignupConfirmPassword.text.toString()
            viewModel.registerFormChanged(
                displayName,
                it,
                password,
                confirmPwd
            )
        }
        binding.editSignupDisplayName.afterTextChanged {
            val email = binding.editSignupEmail.text.toString()
            val password = binding.editSignupPassword.text.toString()
            val confirmPwd = binding.editSignupConfirmPassword.text.toString()
            viewModel.registerFormChanged(it, email, password, confirmPwd)
        }
        binding.editSignupPassword.afterTextChanged {
            val email = binding.editSignupEmail.text.toString()
            val displayName = binding.editSignupDisplayName.text.toString()
            val confirmPwd = binding.editSignupConfirmPassword.text.toString()
            viewModel.registerFormChanged(displayName, email, it, confirmPwd)
        }
        binding.editSignupConfirmPassword.afterTextChanged {
            val email = binding.editSignupEmail.text.toString()
            val displayName = binding.editSignupDisplayName.text.toString()
            val password = binding.editSignupPassword.text.toString()
            viewModel.registerFormChanged(displayName, email, password, it)
        }

        binding.btnSignupFragmentSignup.setOnClickListener {
            ChitChatterUtils.hideKeyBoard(requireView())
            if (isOnline(requireContext())) {
                if (isCorrectForm) {
                    val email = binding.editSignupEmail.text.toString()
                    val displayName = binding.editSignupDisplayName.text.toString()
                    val password = binding.editSignupPassword.text.toString()
                    viewModel.registerAccount(displayName, email, password)
                    binding.progressBarRegister.visibility = View.VISIBLE
                } else {
                    Snackbar.make(
                        binding.root,
                        R.string.message_require_correct_format,
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

        binding.btnSignupFragmentLogin.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViewModel() {
        val repository = (requireActivity().application as ChitChatterApplication).repository

        viewModel = ViewModelProvider(
            requireActivity(),
            RegisterViewModelFactory(repository)
        )[RegisterViewModel::class.java]

        viewModel.registerFormState.observe(viewLifecycleOwner) {
            if (it.displayNameError != null) {
                binding.editSignupDisplayName.error = getString(it.displayNameError)
                isCorrectForm = false
            } else if (it.emailError != null) {
                binding.editSignupEmail.error = getString(it.emailError)
                isCorrectForm = false
            } else if (it.passwordError != null) {
                binding.editSignupPassword.error = getString(it.passwordError)
                isCorrectForm = false
            } else if (it.confirmPasswordError != null) {
                binding.editSignupConfirmPassword.error = getString(it.confirmPasswordError)
                isCorrectForm = false
            } else {
                isCorrectForm = true
            }
        }

        viewModel.registerState.observe(viewLifecycleOwner) {
            if (it.compareTo("Success") != 0) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.msg_check_email_signup,
                    Snackbar.LENGTH_LONG).show()
            }
            binding.progressBarRegister.visibility = View.GONE
        }
    }

    private fun removeForm() {
        binding.editSignupEmail.setText("")
        binding.editSignupPassword.setText("")
        binding.editSignupDisplayName.setText("")
        binding.editSignupConfirmPassword.setText("")
        isCorrectForm = false
    }
}