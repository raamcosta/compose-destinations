package com.ramcosta.destinations.sample.wear.tasks.data

import com.ramcosta.destinations.sample.wear.tasks.domain.Step
import com.ramcosta.destinations.sample.wear.tasks.domain.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class StepsRepository {

    private val nextId = AtomicInteger(0)

    private val _steps = MutableStateFlow<List<Step>>(emptyList())
    val steps = _steps.asStateFlow()

    suspend fun removeStep(step: Step) = withContext(Dispatchers.IO) {
        _steps.update { it - step }
    }

    fun stepsByTask(taskId: Int): Flow<List<Step>> {
        return _steps.map {
            it.filter { it.taskId == taskId }
        }
    }

    suspend fun removeStepsForTask(task: Task) = withContext(Dispatchers.IO) {
        _steps.update {
            it.toMutableList().apply { removeAll { it.taskId == task.id } }
        }
    }

    suspend fun updateStep(step: Step) = withContext(Dispatchers.IO) {
        _steps.update {
            val idx = it.indexOfFirst { it.id == step.id }
            if (idx != -1) {
                it.toMutableList().apply {
                    removeAt(idx)
                    add(idx, step)
                }
            } else {
                it
            }
        }
    }

    suspend fun addNewStep(step: Step) = withContext(Dispatchers.IO) {
        val stepWithRightId = step.copy(id = nextId.getAndAdd(1))
        _steps.update { it + stepWithRightId }
    }

    fun stepForId(stepId: Int): Flow<Step> {
        return _steps.map { it.first { it.id == stepId } }
    }
}