package com.example.sportevents.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportevents.data.models.User
import com.example.sportevents.data.repositories.AuthRepository
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.Resource
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginResult = MutableLiveData<Resource<User>>()
    val loginResult: LiveData<Resource<User>> = _loginResult

    private val _registerResult = MutableLiveData<Resource<User>>()
    val registerResult: LiveData<Resource<User>> = _registerResult

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        _currentUser.value = AuthManager.getCurrentUser()
    }

    fun login(email: String, password: String) {
        _loginResult.value = Resource.Loading

        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            _loginResult.value = result.map { it.user }

            if (result is Resource.Success) {
                _currentUser.value = result.data.user
            }
        }
    }

    fun register(email: String, displayName: String, password: String) {
        _registerResult.value = Resource.Loading

        viewModelScope.launch {
            val result = repository.registerUser(email, displayName, password)
            _registerResult.value = result.map { it.user }

            if (result is Resource.Success) {
                _currentUser.value = result.data.user
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean {
        return AuthManager.isLoggedIn()
    }
}