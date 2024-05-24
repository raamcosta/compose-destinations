package com.ramcosta.destinations.sample.wear.tasks.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.wear.tasks.data.StepsRepository
import com.ramcosta.destinations.sample.wear.tasks.data.TasksRepository
import com.ramcosta.destinations.sample.wear.tasks.domain.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskListViewModel(
    private val tasksRepository: TasksRepository,
    private val stepsRepository: StepsRepository
) : ViewModel() {

    val tasks: StateFlow<List<TaskUiItem>> = tasksRepository.tasks
        .combine(stepsRepository.steps) { tasks, steps ->
            tasks.map { task ->
                TaskUiItem(task, steps.filter { it.taskId == task.id })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onCheckboxChecked(task: Task, checked: Boolean) {
        viewModelScope.launch {
            tasksRepository.updateTask(task.copy(completed = checked))
        }
    }
}