package com.ramcosta.samples.destinationstodosample.ui.screens.profile

import android.util.Log
import kotlinx.coroutines.delay
import org.koin.core.annotation.Factory

@Factory
class GetProfileLikeCountUseCase {

    suspend operator fun invoke(profileId: Long): Int {
        Log.d("GetProfileLikeCount", "Getting like count for profile with id $profileId")
        delay(500) //simulate a network or db call
        return 10
    }
}