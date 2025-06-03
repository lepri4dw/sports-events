package com.example.sportevents.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sportevents.R
import com.example.sportevents.databinding.FragmentLoginBinding
import com.example.sportevents.util.NetworkResult

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.login(email, password)
            } else {
                Toast.makeText(requireContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
                }
                is NetworkResult.Error -> {
                    val errorMessage = when {
                        result.message.contains("Authentication required") -> 
                            "Необходима авторизация. Пожалуйста, войдите в систему."
                        result.message.contains("Authentication error") -> 
                            "Ошибка авторизации. Пожалуйста, войдите снова."
                        result.message.contains("Unable to log in") || 
                        result.message.contains("No active account") -> 
                            "Неверный email или пароль. Пожалуйста, проверьте введенные данные."
                        result.message.contains("Connection timed out") -> 
                            "Превышено время ожидания соединения. Проверьте подключение к интернету."
                        result.message.contains("Unable to connect") -> 
                            "Не удалось подключиться к серверу. Проверьте подключение к интернету."
                        else -> "Ошибка входа: ${result.message}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 