package com.example.saferoute.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val _userLiveData = MutableLiveData<FirebaseUser?>()
    val userLiveData: LiveData<FirebaseUser?> get() = _userLiveData
    private val _errorLiveData = MutableLiveData<String?>()
    val errorLiveData: LiveData<String?> get() = _errorLiveData

    fun login(email: String, password: String) {
        authRepository.loginUser(email, password) { user, error ->
            if (user != null) {
                _userLiveData.postValue(user)
            } else {
                _errorLiveData.postValue(error)
            }
        }
    }

    fun logout() {
        authRepository.logoutUser()
        _userLiveData.postValue(null)
    }
}
