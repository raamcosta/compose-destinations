package com.ramcosta.destinations.sample.tasks.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import com.ramcosta.destinations.sample.tasks.data.StepsRepository
import com.ramcosta.destinations.sample.tasks.data.TasksRepository
import com.ramcosta.destinations.sample.tasks.domain.Step
import com.ramcosta.destinations.sample.tasks.domain.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository,
    private val stepsRepository: StepsRepository
) : ViewModel() {

    private val navArgs: TaskScreenNavArgs = savedStateHandle.navArgs()

    val task: StateFlow<Task?> = tasksRepository.taskById(navArgs.taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val steps: StateFlow<List<Step>> = stepsRepository.stepsByTask(navArgs.taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onStepCheckedChanged(step: Step, checked: Boolean) {
        viewModelScope.launch {
            stepsRepository.updateStep(step.copy(completed = checked))
        }
    }

    fun onTaskCheckedChange(checked: Boolean) {
        val task = task.value ?: return
        viewModelScope.launch {
            tasksRepository.updateTask(task.copy(completed = checked))
        }
    }
}
