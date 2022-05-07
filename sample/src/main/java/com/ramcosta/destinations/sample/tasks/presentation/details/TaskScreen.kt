package com.ramcosta.destinations.sample.tasks.presentation.details

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.destinations.sample.core.viewmodel.viewModel

data class TaskScreenNavArgs(
    val taskId: Int
)

@Destination(navArgsDelegate = TaskScreenNavArgs::class)
@Composable
fun TaskScreen(
    viewModel: TaskDetailsViewModel = viewModel()
) {
    Text("WIP Screen \n ${viewModel.task.collectAsState().value} ")
}
