package com.ramcosta.destinations.sample.tasks.data

import com.ramcosta.destinations.sample.tasks.domain.Step
import com.ramcosta.destinations.sample.tasks.domain.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class StepsRepository {

    private val stepsByTask = mutableMapOf<Int, MutableStateFlow<MutableList<Step>>>()

    suspend fun addStep(step: Step) = withContext(Dispatchers.IO) {
        if (stepsByTask.containsKey(step.taskId)) {
            stepsByTask[step.taskId]!!.apply { value = value.toMutableList().also { it.add(step) } }
            return@withContext
        }

        stepsByTask[step.taskId] = MutableStateFlow(mutableListOf(step))
    }

    suspend fun removeStep(step: Step) = withContext(Dispatchers.IO) {
        val mutableStateFlow = stepsByTask[step.taskId]
        mutableStateFlow?.value?.let { list ->
            mutableStateFlow.value = list.toMutableList().also { it.remove(step) }
        }
    }

    fun stepsByTask(task: Task): Flow<List<Step>> {
        return stepsByTask[task.id]
            ?: MutableStateFlow<MutableList<Step>>(mutableListOf())
                .also { stepsByTask[task.id] = it }
    }

    suspend fun removeStepsForTask(task: Task) = withContext(Dispatchers.IO) {
        stepsByTask[task.id]?.let { state ->
            state.update { it.toMutableList().apply { clear() } }
        }
    }
}