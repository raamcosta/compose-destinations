package com.ramcosta.samples.playground.ui.screens.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.samples.playground.ui.screens.navArgs
import kotlinx.coroutines.launch

class ProfileViewModel(
    getProfileLikeCount: GetProfileLikeCountUseCase,
    handle: SavedStateHandle
) : ViewModel(), ProfileUiEvents, ProfileUiState {

    private val navArgs = handle.navArgs<ProfileScreenNavArgs>()

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