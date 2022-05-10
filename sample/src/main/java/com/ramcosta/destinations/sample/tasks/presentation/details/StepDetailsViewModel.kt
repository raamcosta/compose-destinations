package com.ramcosta.destinations.sample.tasks.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.tasks.data.StepsRepository
import com.ramcosta.destinations.sample.tasks.data.TasksRepository
import com.ramcosta.destinations.sample.tasks.domain.Step
import com.ramcosta.destinations.sample.tasks.domain.Task
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StepDetailsViewModel(
    private val stepId: Int,
    private val tasksRepository: TasksRepository,
    private val stepsRepository: StepsRepository
): ViewModel() {

    val step: StateFlow<Step?> = stepsRepository.stepForId(stepId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val task: StateFlow<Task?> = stepsRepository.stepForId(stepId)
        .flatMapConcat {
            tasksRepository.taskById(it.taskId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onStepCheckedChange(checked: Boolean) {
        val value = step.value ?: return
        viewModelScope.launch {
            stepsRepository.updateStep(value.copy(completed = checked))
        }
    }
}