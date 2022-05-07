package com.ramcosta.destinations.sample.tasks.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.navArgs
import com.ramcosta.destinations.sample.tasks.data.StepsRepository
import com.ramcosta.destinations.sample.tasks.data.TasksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class TaskDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository,
    private val stepsRepository: StepsRepository
) : ViewModel() {

    private val navArgs: TaskScreenNavArgs = savedStateHandle.navArgs()

    val task = tasksRepository.taskById(navArgs.taskId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
}
