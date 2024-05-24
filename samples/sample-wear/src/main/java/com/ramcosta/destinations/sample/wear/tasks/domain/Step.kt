package com.ramcosta.destinations.sample.wear.tasks.domain

data class Step(
    val taskId: Int,
    val title: String,
    val completed: Boolean,
    val id: Int = 0,
)
