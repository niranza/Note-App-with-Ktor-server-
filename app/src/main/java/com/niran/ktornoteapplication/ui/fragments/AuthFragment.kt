package com.niran.ktornoteapplication.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.niran.ktornoteapplication.R
import com.niran.ktornoteapplication.databinding.FragmentAuthBinding
import com.niran.ktornoteapplication.dataset.retrofit.BasicAuthInterceptor
import com.niran.ktornoteapplication.utils.Constants
import com.niran.ktornoteapplication.utils.Constants.KEY_LOGGED_IN_EMAIL
import com.niran.ktornoteapplication.utils.Constants.KEY_PASSWORD
import com.niran.ktornoteapplication.utils.Constants.NO_EMAIL
import com.niran.ktornoteapplication.utils.Constants.NO_PASSWORD
import com.niran.ktornoteapplication.utils.FragmentUtils.lockFragmentRotation
import com.niran.ktornoteapplication.utils.FragmentUtils.showSnackBar
import com.niran.ktornoteapplication.utils.Resource
import com.niran.ktornoteapplication.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    private var currentEmail: String? = null
    private var currentPassword: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAuthBinding.inflate(inflater)

        lockFragmentRotation()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (alreadyLoggedIn()) {
            authenticateApi(currentEmail ?: "", currentPassword ?: "")
            navigateToNotesFragment()
        }

        setBtnRegister()
        setBtnLogin()
        setObservers()
    }

    private fun setObservers() = binding.apply {
        viewModel.registerStatus.observe(viewLifecycleOwner) { status ->
            status?.let {
                when (it) {
                    is Resource.Loading -> {
                        showRegisterLoading()
                    }
                    is Resource.Success -> {
                        hideRegisterLoading()
                        showSnackBar(
                            it.data ?: getString(R.string.successfully_registered),
                            root
                        )
                    }
                    is Resource.Error -> {
                        hideRegisterLoading()
                        showSnackBar(it.message ?: getString(R.string.unknown_error), root)
                    }
                }
            }
        }

        viewModel.loginStatus.observe(viewLifecycleOwner) { status ->
            status?.let {
                when (it) {
                    is Resource.Loading -> showLoginLoading()
                    is Resource.Success -> {
                        hideLoginLoading()
                        showSnackBar(
                            it.message ?: getString(R.string.successfully_logged_in),
                            root
                        )
                        sharedPref.edit().apply {
                            putString(KEY_LOGGED_IN_EMAIL, currentEmail)
                            putString(Constants.KEY_PASSWORD, currentPassword)
                            apply()
                        }
                        authenticateApi(currentEmail ?: "", currentPassword ?: "")
                        navigateToNotesFragment()
                    }
                    is Resource.Error -> {
                        hideLoginLoading()
                        showSnackBar(it.message ?: getString(R.string.unknown_error), root)
                    }
                }
            }
        }
    }

    private fun setBtnLogin() = binding.apply {
        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString()
            val password = etLoginPassword.text.toString()
            currentEmail = email
            currentPassword = password
            viewModel.login(email, password)
        }
    }

    private fun setBtnRegister() = binding.apply {
        btnRegister.setOnClickListener {
            val email = etRegisterEmail.text.toString()
            val password = etRegisterPassword.text.toString()
            val confirmedPassword = etRegisterPasswordConfirm.text.toString()
            viewModel.register(email, password, confirmedPassword)
        }
    }

    private fun showRegisterLoading() = binding.apply { pbRegister.visibility = View.VISIBLE }

    private fun hideRegisterLoading() = binding.apply { pbRegister.visibility = View.GONE }

    private fun showLoginLoading() = binding.apply { pbLogin.visibility = View.VISIBLE }

    private fun hideLoginLoading() = binding.apply { pbLogin.visibility = View.GONE }

    private fun authenticateApi(email: String, password: String) {
        basicAuthInterceptor.email = email
        basicAuthInterceptor.password = password
    }

    private fun alreadyLoggedIn(): Boolean {
        currentEmail = sharedPref.getString(KEY_LOGGED_IN_EMAIL, NO_EMAIL) ?: NO_EMAIL
        currentPassword = sharedPref.getString(KEY_PASSWORD, NO_PASSWORD) ?: NO_PASSWORD
        return currentEmail != NO_EMAIL && currentPassword != NO_PASSWORD
    }

    private fun navigateToNotesFragment() = view?.findNavController()
        ?.navigate(AuthFragmentDirections.actionAuthFragmentToNotesFragment())

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



