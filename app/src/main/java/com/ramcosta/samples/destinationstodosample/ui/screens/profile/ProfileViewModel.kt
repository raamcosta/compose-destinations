package com.ramcosta.samples.destinationstodosample.ui.screens.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.samples.destinationstodosample.ui.screens.destinations.ProfileScreenDestination
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel(binds = [ProfileUiEvents::class, ProfileUiState::class])
class ProfileViewModel(
    getProfileLikeCount: GetProfileLikeCountUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ProfileUiEvents, ProfileUiState {

    private val navArgs: ProfileScreenNavArgs = kotlin.runCatching { ProfileScreenDestination.argsFrom(savedStateHandle) }
        .getOrElse {
            // Hilt currently has this issue where it is not passing the bundle from navigation to SavedStateHandle
            // I did not find a way to do this (I only did a small research though)
            ProfileScreenNavArgs(3, color= Color.DarkGray, things = Things(), thingsWithNavTypeSerializer = Things())
        }

    init {
        Log.d("ProfileViewModel", "navArgs= $navArgs")
    }

    override val groupName: String = navArgs.groupName?.ifEmpty { "user doesn't belong to any group" } ?: "null"

    override val id: Long = navArgs.id

    override var likeCount: Int by mutableStateOf(0)

    init {
        viewModelScope.launch {
            likeCount = getProfileLikeCount(id)
        }
    }

    override fun onLikeButtonClick() {
        likeCount++
    }
}