package com.ramcosta.destinations.sample.tasks.domain

data class Step(
    val id: Int,
    val taskId: Int,
    val title: String,
    val completed: Boolean
)
