package com.ramcosta.destinations.sample.wear.tasks.presentation.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.destinations.sample.wear.core.viewmodel.viewModel
import com.ramcosta.destinations.sample.wear.destinations.AddStepScreenDestination
import com.ramcosta.destinations.sample.wear.destinations.StepScreenDestination
import com.ramcosta.destinations.sample.wear.tasks.domain.Step

@Destination(navArgsDelegate = TaskScreenNavArgs::class)
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

    val steps = viewModel.steps.collectAsState().value

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Button(
                onClick = { navigator.navigate(AddStepScreenDestination(navArgs.taskId)) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add step button"
                )
            }
        }

        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = task.completed,
                onCheckedChange = viewModel::onTaskCheckedChange,
                label = { Text("Completed:") },
                toggleControl = {
                    Icon(
                        imageVector = ToggleChipDefaults.checkboxIcon(checked = task.completed),
                        contentDescription = if (task.completed) "On" else "Off"
                    )
                }
            )
        }

        item {
            ListHeader {
                Text("Steps")
            }
        }

        if (steps.isEmpty()) {
            item {
                Text("None", style = MaterialTheme.typography.body2)
            }
        }

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

@Composable
fun StepItem(
    step: Step,
    onStepClicked: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    SplitToggleChip(
        modifier = Modifier.fillMaxWidth(),
        onClick = onStepClicked,
        checked = step.completed,
        onCheckedChange = onCheckedChange,
        label = { Text(step.title) },
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.checkboxIcon(checked = step.completed),
                contentDescription = if (step.completed) "On" else "Off"
            )
        }
    )
}
