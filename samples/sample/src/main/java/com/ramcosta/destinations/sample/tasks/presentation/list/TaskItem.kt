package com.ramcosta.destinations.sample.tasks.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TaskItem(
    task: TaskUiItem,
    onCheckedChange: (Boolean) -> Unit,
    onTaskClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.task.completed,
            onCheckedChange = onCheckedChange
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(task.task.title)

        if (task.steps.isNotEmpty()) {
            Spacer(modifier = Modifier.weight(1f))

            Text("${task.steps.filter { it.completed }.size}/${task.steps.size}")
        }
    }
}