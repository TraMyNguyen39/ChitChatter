package com.midterm.chitchatter.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.midterm.chitchatter.ChitChatterApplication
import com.midterm.chitchatter.R
import com.midterm.chitchatter.databinding.FragmentLoginBinding
import com.midterm.chitchatter.ui.MainActivity
import com.midterm.chitchatter.utils.ChitChatterUtils
import com.midterm.chitchatter.utils.ChitChatterUtils.afterTextChanged
import com.midterm.chitchatter.utils.ChitChatterUtils.isOnline

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    private var isCorrectForm = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
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
    private fun setupListener() {
        binding.editLoginEmail.afterTextChanged {
            val password = binding.editLoginPassword.text.toString()
            viewModel.loginFormChange(it, password)
        }

        binding.editLoginPassword.afterTextChanged {
            val username = binding.editLoginEmail.text.toString()
            viewModel.loginFormChange(username, it)
        }

        binding.btnSignIn.setOnClickListener {
            ChitChatterUtils.hideKeyBoard(requireView())
            if (isOnline(requireContext())) {
                binding.progressBarLogin.visibility = View.VISIBLE
                if (isCorrectForm) {
                    val email = binding.editLoginEmail.text.toString()
                    val password = binding.editLoginPassword.text.toString()
                    viewModel.login(email, password)
                } else {
                    Snackbar.make(
                        binding.root,
                        R.string.message_wrong_email_password,
                        Snackbar.LENGTH_LONG
                    ).show()
                    binding.progressBarLogin.visibility = View.GONE
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.message_no_internet,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        binding.btnSignUp.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }

        binding.btnForgetPassword.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToForgetPasswordFragment()
            findNavController().navigate(action)
        }
    }

    private fun setUpViewModel() {
        val repository = (requireActivity().application as ChitChatterApplication).repository

        viewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory(repository)
        )[LoginViewModel::class.java]

        viewModel.loginFormState.observe(viewLifecycleOwner) {
            if (it.emailError != null) {
                binding.editLoginEmail.error = getString(it.emailError)
            }
            isCorrectForm = it.isCorrect
        }

        viewModel.loggedInAccount.observe(viewLifecycleOwner) { account ->
            if (account != null) {
                if (account.isVerified!!) {
                    Snackbar.make(binding.root, "Welcome ${account.name}", Snackbar.LENGTH_LONG)
                        .show()
                    // Lưu account vào preference
                    saveToCurrentAccountPreference(account.email, account.name)
                    // Lưu vào preference rồi thì reset lại loggedInAccount thôi
                    viewModel.resetAccount()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                } else {
                    viewModel.sendEmailVerification(account.email)
                    Snackbar.make(binding.root, R.string.msg_check_email_to_verify, Snackbar.LENGTH_LONG)
                        .show()
                }
            } else {
                if (isCorrectForm) { // check lậu truong hop reset lam null co thong bao
                    Snackbar.make(
                        binding.root,
                        R.string.message_wrong_email_password,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            binding.progressBarLogin.visibility = View.GONE
        }
    }

    private fun saveToCurrentAccountPreference (
        email: String, displayName: String, imageUrl: String? = null
    ) {
        val sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.preference_account_key), Context.MODE_PRIVATE)

        with (sharedPref.edit()) {
            putString(getString(R.string.preference_email_key), email)
            putString(getString(R.string.preference_display_name_key), displayName)
//            putString(getString(R.string.preference_dislay_name_key), imageUrl)
            apply()
        }
    }

    private fun removeForm() {
        binding.editLoginEmail.setText("")
        binding.editLoginPassword.setText("")
        isCorrectForm = false
    }
}