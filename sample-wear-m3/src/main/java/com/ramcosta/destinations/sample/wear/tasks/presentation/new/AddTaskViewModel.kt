package com.ramcosta.destinations.sample.wear.tasks.presentation.new

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.wear.tasks.data.TasksRepository
import com.ramcosta.destinations.sample.wear.tasks.domain.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTaskViewModel(
    private val tasksRepository: TasksRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _title.update { newTitle }
    }

    fun onConfirmClicked() {
        viewModelScope.launch {
            tasksRepository.addNewTask(Task(_title.value, "", false))
        }
    }
}
