package com.ramcosta.samples.destinationstodosample.ui.screens.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import com.ramcosta.samples.destinationstodosample.ui.screens.destinations.ProfileScreenDestination

class ProfileViewModel(
    navArgs: ProfileScreenNavArgs
) : ViewModel(), ProfileUiEvents, ProfileUiState {

    init {
        Log.d("ProfileViewModel", "navArgs= $navArgs")
    }

    override val groupName: String = navArgs.groupName ?: "user doesn't belong to any group"

    override val id: Long = navArgs.id

    override var likeCount: Int by mutableStateOf(0)

    override fun onLikeButtonClick() {
        likeCount++
    }

    class Factory(
        navBackStackEntry: NavBackStackEntry
    ) : AbstractSavedStateViewModelFactory(
        navBackStackEntry,
        navBackStackEntry.arguments
    ) {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return ProfileViewModel(
                /*
                ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                    HEY!!!
                    I'm here as an example of how you can get nav arguments
                    in ViewModels =)
                ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                */
                ProfileScreenDestination.argsFrom(handle)
            ) as T
        }
    }
}