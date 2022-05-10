package com.ramcosta.destinations.sample.login.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class LoginStateRepository {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    suspend fun login() = withContext(Dispatchers.IO) {
        _isLoggedIn.update { true }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        _isLoggedIn.update { false }
    }
}