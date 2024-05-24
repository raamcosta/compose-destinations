package com.ramcosta.destinations.sample.wear.tasks.presentation.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.SplitToggleChip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChipDefaults

@Composable
fun TaskItem(
    task: TaskUiItem,
    onCheckedChange: (Boolean) -> Unit,
    onTaskClicked: () -> Unit
) {
    SplitToggleChip(
        modifier = Modifier.fillMaxWidth(),
        onClick = onTaskClicked,
        checked = task.task.completed,
        onCheckedChange = onCheckedChange,
        label = {
            Text(task.task.title)
        },
        toggleControl = {
            Icon(
                imageVector = ToggleChipDefaults.checkboxIcon(checked = task.task.completed),
                contentDescription = if (task.task.completed) "On" else "Off"
            )
        },
        secondaryLabel = if (task.steps.isNotEmpty()) {
            {
                Text("${task.steps.filter { it.completed }.size}/${task.steps.size}")
            }
        } else null,
    )
}