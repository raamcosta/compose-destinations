package com.ramcosta.destinations.sample.tasks.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.generated.destinations.AddTaskDialogDestination
import com.ramcosta.composedestinations.generated.destinations.TaskScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.destinations.sample.core.viewmodel.viewModel

@RootNavGraph(start = true)
@Destination
@Composable
fun TaskListScreen(
    navigator: DestinationsNavigator,
    viewModel: TaskListViewModel = viewModel()
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigator.navigate(AddTaskDialogDestination) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add task button",
                    tint = Color.White
                )
            }
        }
    ){

        val tasks by viewModel.tasks.collectAsState()

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(it)
        ) {
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
}
