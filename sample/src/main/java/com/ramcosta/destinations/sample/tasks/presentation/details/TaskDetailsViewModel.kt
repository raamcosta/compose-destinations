package com.ramcosta.destinations.sample.tasks.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.tasks.data.StepsRepository
import com.ramcosta.destinations.sample.tasks.data.TasksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class TaskDetailsViewModel(
    private val taskId: Int,
    private val tasksRepository: TasksRepository,
    private val stepsRepository: StepsRepository
) : ViewModel() {

    val task = tasksRepository.taskById(taskId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
}
