package com.ramcosta.samples.destinationstodosample.ui.screens.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileViewModel(
    getProfileLikeCount: GetProfileLikeCountUseCase,
    navArgs: ProfileScreenNavArgs
) : ViewModel(), ProfileUiEvents, ProfileUiState {

    init {
        Log.d("ProfileViewModel", "navArgs= $navArgs")
    }

    override val groupName: String = navArgs.groupName.ifEmpty { "user doesn't belong to any group" }

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