package com.example.nadziko.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nadziko.data.SessionManager
import com.example.nadziko.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginState: StateFlow<LoginResult> = _loginState

    fun register(username: String, password: String) {
        viewModelScope.launch {
            val existingUser = repository.getUserByUsername(username)
            if (existingUser != null) {
                _loginState.value = LoginResult.Error("Użytkownik o takiej nazwie już istnieje")
            } else {
                repository.insertUser(username, password)
                val newUser = repository.getUserByUsername(username)
                if (newUser != null) {
                    sessionManager.saveSession(newUser.id, newUser.username)
                    _loginState.value = LoginResult.Success
                }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null && user.passwordHash == password) {
                sessionManager.saveSession(user.id, user.username)
                _loginState.value = LoginResult.Success
            } else {
                _loginState.value = LoginResult.Error("Błędna nazwa użytkownika lub hasło")
            }
        }
    }

    fun logout() {
        sessionManager.logout()
        _loginState.value = LoginResult.Idle
    }

    fun resetState() {
        _loginState.value = LoginResult.Idle
    }
}

sealed class LoginResult {
    object Idle : LoginResult()
    object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class UserViewModelFactory(
    private val repository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
