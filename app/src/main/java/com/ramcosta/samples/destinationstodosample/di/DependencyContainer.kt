package com.ramcosta.samples.destinationstodosample.di

import com.ramcosta.samples.destinationstodosample.ui.screens.profile.GetProfileLikeCountUseCase

class DependencyContainer {
    val getProfileLikeCount get() = GetProfileLikeCountUseCase()
}
