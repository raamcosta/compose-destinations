package com.ramcosta.destinations.sample.wear.tasks.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.items
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.destinations.sample.wear.core.viewmodel.viewModel
import com.ramcosta.destinations.sample.wear.destinations.AccountScreenDestination
import com.ramcosta.destinations.sample.wear.destinations.AddTaskScreenDestination
import com.ramcosta.destinations.sample.wear.destinations.SettingsScreenDestination
import com.ramcosta.destinations.sample.wear.destinations.TaskScreenDestination

@RootGraph(start = true)
@Destination
@Composable
fun TaskListScreen(
    navigator: DestinationsNavigator,
    viewModel: TaskListViewModel = viewModel()
) {
    val tasks by viewModel.tasks.collectAsState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row {
                Button(
                    onClick = { navigator.navigate(AddTaskScreenDestination) }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "add task button")
                }

                Button(onClick = { navigator.navigate(AccountScreenDestination) }) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Account")
                }

                Button(onClick = { navigator.navigate(SettingsScreenDestination) }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }
        items(tasks) { item ->
            TaskItem(
                task = item,
                onCheckedChange = {
                    viewModel.onCheckboxChecked(item.task, it)
                },
                onTaskClicked = {
                    navigator.navigate(TaskScreenDestination(item.task.id))
                }
            )
        }
    }
}
