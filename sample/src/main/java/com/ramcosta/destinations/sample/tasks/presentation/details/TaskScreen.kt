package com.ramcosta.destinations.sample.tasks.presentation.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.destinations.sample.core.viewmodel.viewModel
import com.ramcosta.destinations.sample.destinations.AddStepDialogDestination
import com.ramcosta.destinations.sample.destinations.StepScreenDestination
import com.ramcosta.destinations.sample.tasks.domain.Step

@Destination(navArgs = TaskScreenNavArgs::class)
@Composable
fun TaskScreen(
    navArgs: TaskScreenNavArgs,
    navigator: DestinationsNavigator,
    viewModel: TaskDetailsViewModel = viewModel()
) {
    val task = viewModel.task.collectAsState().value

    if (task == null) {
        CircularProgressIndicator()
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigator.navigate(AddStepDialogDestination(navArgs.taskId)) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add step button",
                    tint = Color.White
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Completed:")

                Checkbox(
                    checked = task.completed,
                    onCheckedChange = viewModel::onTaskCheckedChange
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("Steps:")

            val steps = viewModel.steps.collectAsState().value
            LazyColumn {
                items(steps) { step ->
                    StepItem(
                        step = step,
                        onStepClicked = {
                            navigator.navigate(StepScreenDestination(step.id))
                        },
                        onCheckedChange = { viewModel.onStepCheckedChanged(step, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun StepItem(
    step: Step,
    onStepClicked: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStepClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = step.completed,
            onCheckedChange = onCheckedChange
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(step.title)
    }
}
