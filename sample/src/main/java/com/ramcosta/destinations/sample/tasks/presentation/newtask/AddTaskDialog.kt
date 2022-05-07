package com.ramcosta.destinations.sample.tasks.presentation.newtask

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.destinations.sample.core.viewmodel.viewModel

@Destination(style = DestinationStyle.Dialog::class)
@Composable
fun AddTaskDialog(
    navigator: DestinationsNavigator,
    viewModel: AddTaskViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Absolute.spacedBy(8.dp)
    ) {
        Text("Add a new task:")

        OutlinedTextField(
            placeholder = { Text("Task title")},
            value = viewModel.title.collectAsState().value,
            onValueChange = viewModel::onTitleChange
        )

        Button(
            onClick = {
                viewModel.onConfirmClicked()
                navigator.popBackStack()
            }
        ) {
            Text("Confirm")
        }
    }
}