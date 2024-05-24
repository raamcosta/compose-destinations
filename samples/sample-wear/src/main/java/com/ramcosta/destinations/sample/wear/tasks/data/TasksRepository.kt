package com.ramcosta.destinations.sample.wear.tasks.data

import com.ramcosta.destinations.sample.wear.tasks.domain.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class TasksRepository(
    private val stepsRepository: StepsRepository
) {

    private val _tasks: MutableStateFlow<MutableList<Task>> = MutableStateFlow<MutableList<Task>>(mutableListOf()).apply {
        value = mutableListOf(
            Task("Task #1", "Description #1", false, 1),
            Task("Task #2", "Description #2", false, 2),
            Task("Task #3", "Description #3", false, 3),
            Task("Task #4", "Description #4", false, 4),
            Task("Task #5", "Description #5", false, 5),
            Task("Task #6", "Description #6", false, 6),
        )
    }
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun taskById(id: Int): Flow<Task?> = _tasks.map { it.firstOrNull { it.id == id } }

    suspend fun addNewTask(task: Task) = withContext(Dispatchers.IO) {
        _tasks.update {
            it.toMutableList().apply {
                add(task.copy(id = (it.lastOrNull()?.id ?: -1) + 1))
            }
        }
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        _tasks.update {
            it.toMutableList().apply {
                val idx = indexOfFirst {  it.id == task.id  }
                if (idx != -1) {
                    removeAt(idx)
                    add(idx, task)
                }
            }
        }
    }

    suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        _tasks.update {
            it.toMutableList().apply { remove(task) }
        }

        stepsRepository.removeStepsForTask(task)
    }
}