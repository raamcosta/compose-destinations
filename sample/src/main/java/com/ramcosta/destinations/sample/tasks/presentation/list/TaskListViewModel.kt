package com.ramcosta.destinations.sample.tasks.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.tasks.data.StepsRepository
import com.ramcosta.destinations.sample.tasks.data.TasksRepository
import com.ramcosta.destinations.sample.tasks.domain.Task
import kotlinx.coroutines.flow.*
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