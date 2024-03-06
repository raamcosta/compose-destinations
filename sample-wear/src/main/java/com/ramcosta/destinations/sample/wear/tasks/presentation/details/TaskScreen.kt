package com.ramcosta.destinations.sample.wear.tasks.presentation.details

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.SplitToggleChip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.items
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AddStepScreenDestination
import com.ramcosta.composedestinations.generated.destinations.StepScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.destinations.sample.wear.core.viewmodel.viewModel
import com.ramcosta.destinations.sample.wear.tasks.domain.Step

@Destination<RootGraph>(navArgs = TaskScreenNavArgs::class)
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
