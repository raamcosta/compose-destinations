package com.ramcosta.destinations.sample.wear.tasks.presentation.list

import com.ramcosta.destinations.sample.wear.tasks.domain.Step
import com.ramcosta.destinations.sample.wear.tasks.domain.Task

data class TaskUiItem(
    val task: Task,
    val steps: List<Step>
)