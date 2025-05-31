package com.example.sportevents.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportevents.data.models.User
import com.example.sportevents.data.repositories.AuthRepository
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val TAG = "AuthViewModel"
    private val repository = AuthRepository()

    private val _loginResult = MutableLiveData<NetworkResult<User>>()
    val loginResult: LiveData<NetworkResult<User>> = _loginResult

    private val _registerResult = MutableLiveData<NetworkResult<User>>()
    val registerResult: LiveData<NetworkResult<User>> = _registerResult

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        _currentUser.value = AuthManager.getCurrentUser()
    }

    fun login(email: String, password: String) {
        _loginResult.value = NetworkResult.Loading
        Log.d(TAG, "Logging in with email: $email")

        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            
            when (result) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Login successful, getting profile")
                    // After successful login, fetch user profile to ensure we have current data
                    val userResult = repository.getCurrentUser()
                    when (userResult) {
                        is NetworkResult.Success -> {
                            Log.d(TAG, "Profile fetch successful")
                            // Update the stored user with complete profile info
                            AuthManager.saveUser(userResult.data)
                            _currentUser.value = userResult.data
                            _loginResult.value = NetworkResult.Success(userResult.data)
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Failed to fetch profile: ${userResult.message}")
                            // Still consider login successful but use the user from login
                            _currentUser.value = result.data.user
                            _loginResult.value = NetworkResult.Success(result.data.user)
                        }
                        is NetworkResult.Loading -> { /* Should not happen */ }
                    }
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Login failed: ${result.message}")
                    _loginResult.value = NetworkResult.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _loginResult.value = NetworkResult.Loading
                }
            }
        }
    }

    fun register(email: String, displayName: String, password: String) {
        _registerResult.value = NetworkResult.Loading
        Log.d(TAG, "Registering with email: $email")

        viewModelScope.launch {
            val result = repository.registerUser(email, displayName, password)
            
            when (result) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Registration successful, getting profile")
                    // After successful registration, fetch user profile
                    val userResult = repository.getCurrentUser()
                    when (userResult) {
                        is NetworkResult.Success -> {
                            Log.d(TAG, "Profile fetch successful")
                            // Update the stored user with complete profile info
                            AuthManager.saveUser(userResult.data)
                            _currentUser.value = userResult.data
                            _registerResult.value = NetworkResult.Success(userResult.data)
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Failed to fetch profile: ${userResult.message}")
                            // Still consider registration successful
                            _currentUser.value = result.data.user
                            _registerResult.value = NetworkResult.Success(result.data.user)
                        }
                        is NetworkResult.Loading -> { /* Should not happen */ }
                    }
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Registration failed: ${result.message}")
                    _registerResult.value = NetworkResult.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _registerResult.value = NetworkResult.Loading
                }
            }
        }
    }

    fun logout() {
        Log.d(TAG, "Logging out")
        repository.logout()
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean {
        return AuthManager.isLoggedIn()
    }
}