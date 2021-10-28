package com.ramcosta.samples.destinationstodosample.destinations.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.ProfileScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel(), ProfileUiEvents, ProfileUiState {

    private val navArgs: ProfileScreenNavArgs = ProfileScreenDestination.argsFrom(savedStateHandle)

    init {
        Log.d("ProfileViewModel", "navArgs= $navArgs")
    }

    override val groupName: String = navArgs.groupName ?: "user doesn't belong to any group"

    override val id: Long = navArgs.id

    override var likeCount: Int by mutableStateOf(0)

    override fun onLikeButtonClick() {
        likeCount++
    }

}