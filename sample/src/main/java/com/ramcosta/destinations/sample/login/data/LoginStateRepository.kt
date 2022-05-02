package com.ramcosta.destinations.sample.login.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginStateRepository {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    suspend fun login() {
        delay(500)
        _isLoggedIn.update { true }
    }

    suspend fun logout() {
        delay(500)
        _isLoggedIn.update { false }
    }
}