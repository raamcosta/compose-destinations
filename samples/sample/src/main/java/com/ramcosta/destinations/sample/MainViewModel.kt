package com.ramcosta.destinations.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.login.data.LoginStateRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val loginStateRepository: LoginStateRepository
) : ViewModel() {

    val isLoggedInFlow = loginStateRepository.isLoggedIn

    val isLoggedIn get() = isLoggedInFlow.value

    fun login() {
        viewModelScope.launch {
            loginStateRepository.login()
        }
    }
}