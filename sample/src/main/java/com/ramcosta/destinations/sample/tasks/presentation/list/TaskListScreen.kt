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
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.destinations.sample.core.viewmodel.viewModel
import com.ramcosta.destinations.sample.destinations.AddTaskDialogDestination
import com.ramcosta.destinations.sample.destinations.LoginScreenDestination
import com.ramcosta.destinations.sample.destinations.SettingsScreenDestination
import com.ramcosta.destinations.sample.destinations.TaskScreenDestination

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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = { navigator.navigate(LoginScreenDestination) }) {
                    Text("Login")
                }
                Button(onClick = { navigator.navigate(SettingsScreenDestination) }) {
                    Text("Settings")
                }
            }

            val tasks by viewModel.tasks.collectAsState()

            LazyColumn {
                items(tasks) { item ->
                    TaskItem(
                        task = item,
                        onCheckedChange = {
                            viewModel.onCheckboxChecked(item, it)
                        },
                        onTaskClicked = {
                            navigator.navigate(TaskScreenDestination(item.id))
                        }
                    )
                }
            }
        }
    }
}
