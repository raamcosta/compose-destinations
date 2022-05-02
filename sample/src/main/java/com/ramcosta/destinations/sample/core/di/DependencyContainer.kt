package com.ramcosta.destinations.sample.core.di

import com.ramcosta.destinations.sample.MainActivity
import com.ramcosta.destinations.sample.login.data.LoginStateRepository

class DependencyContainer(
    val activity: MainActivity
) {

    val loginStateRepository: LoginStateRepository by lazy { LoginStateRepository() }
}