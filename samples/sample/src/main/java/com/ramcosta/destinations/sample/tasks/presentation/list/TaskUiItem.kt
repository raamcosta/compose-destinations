package com.ramcosta.destinations.sample.tasks.presentation.list

import com.ramcosta.destinations.sample.tasks.domain.Step
import com.ramcosta.destinations.sample.tasks.domain.Task

data class TaskUiItem(
    val task: Task,
    val steps: List<Step>
)