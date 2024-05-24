package com.ramcosta.samples.playground.ui.screens.profile

import android.util.Log
import kotlinx.coroutines.delay

class GetProfileLikeCountUseCase {

    suspend operator fun invoke(profileId: Long): Int {
        Log.d("GetProfileLikeCount", "Getting like count for profile with id $profileId")
        delay(500) //simulate a network or db call
        return 10
    }
}