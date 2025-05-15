package com.ramcosta.destinations.sample.wear.tasks.presentation.details

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SplitCheckboxButton
import androidx.wear.compose.material3.Text
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

    val listState = rememberScalingLazyListState()
    ScreenScaffold(scrollState = listState) { contentPadding ->
        ScalingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
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
                CheckboxButton(
                    modifier = Modifier.fillMaxWidth(),
                    checked = task.completed,
                    onCheckedChange = viewModel::onTaskCheckedChange,
                    label = { Text("Completed:") },
                )
            }

            item {
                ListHeader {
                    Text("Steps")
                }
            }

            if (steps.isEmpty()) {
                item {
                    Text("None", style = MaterialTheme.typography.bodyMedium)
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
}

@Composable
fun StepItem(
    step: Step,
    onStepClicked: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    SplitCheckboxButton(
        modifier = Modifier.fillMaxWidth(),
        onContainerClick = onStepClicked,
        checked = step.completed,
        onCheckedChange = onCheckedChange,
        label = { Text(step.title) },
        toggleContentDescription = "step toggle"
    )
}
