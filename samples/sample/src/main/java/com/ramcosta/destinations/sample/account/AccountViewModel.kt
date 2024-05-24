package com.ramcosta.destinations.sample.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.login.data.LoginStateRepository
import kotlinx.coroutines.launch

class AccountViewModel(
    private val loginStateRepository: LoginStateRepository
) : ViewModel() {

    fun onLogoutClick() {
        viewModelScope.launch {
            loginStateRepository.logout()
        }
    }
}