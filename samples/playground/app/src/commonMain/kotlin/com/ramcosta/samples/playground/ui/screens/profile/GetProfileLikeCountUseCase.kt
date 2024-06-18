package com.ramcosta.samples.playground.ui.screens.profile

import kotlinx.coroutines.delay

class GetProfileLikeCountUseCase {

    suspend operator fun invoke(profileId: Long): Int {
        println("GetProfileLikeCount | Getting like count for profile with id $profileId")
        delay(500) //simulate a network or db call
        return 10
    }
}