package com.ramcosta.destinations.sample.wear.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.wear.login.data.LoginStateRepository
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