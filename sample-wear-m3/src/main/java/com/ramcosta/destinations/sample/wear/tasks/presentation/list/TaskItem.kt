package com.ramcosta.destinations.sample.wear.tasks.presentation.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.SplitCheckboxButton
import androidx.wear.compose.material3.Text

@Composable
fun TaskItem(
    task: TaskUiItem,
    onCheckedChange: (Boolean) -> Unit,
    onTaskClicked: () -> Unit
) {
    SplitCheckboxButton(
        modifier = Modifier.fillMaxWidth(),
        onContainerClick = onTaskClicked,
        checked = task.task.completed,
        onCheckedChange = onCheckedChange,
        label = {
            Text(task.task.title)
        },
        secondaryLabel = if (task.steps.isNotEmpty()) {
            {
                Text("${task.steps.filter { it.completed }.size}/${task.steps.size}")
            }
        } else null,
        toggleContentDescription = "task toggle"
    )
}