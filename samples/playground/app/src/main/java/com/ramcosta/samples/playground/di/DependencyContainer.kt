package com.ramcosta.samples.playground.di

import com.ramcosta.samples.playground.ui.screens.profile.GetProfileLikeCountUseCase

class DependencyContainer {
    val getProfileLikeCount get() = GetProfileLikeCountUseCase()
}
