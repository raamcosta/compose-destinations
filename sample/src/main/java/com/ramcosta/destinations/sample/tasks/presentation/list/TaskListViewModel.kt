package com.ramcosta.destinations.sample.tasks.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.tasks.data.TasksRepository
import com.ramcosta.destinations.sample.tasks.domain.Task
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskListViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = tasksRepository.tasks

    fun onCheckboxChecked(task: Task, checked: Boolean) {
        viewModelScope.launch {
            tasksRepository.updateTask(task.copy(completed = checked))
        }
    }
}