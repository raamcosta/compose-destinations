package com.ramcosta.destinations.sample.wear.tasks.presentation.new

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.destinations.sample.wear.core.viewmodel.viewModel
import com.ramcosta.destinations.sample.wear.ui.composables.TitleConfirmDialog

@Destination
@Composable
fun AddTaskScreen(
    navigator: DestinationsNavigator,
    viewModel: AddTaskViewModel = viewModel()
) {
    TitleConfirmDialog(
        type = "task", //use string resources in a real app ofc :)
        title = viewModel.title.collectAsState().value,
        onTitleChange = viewModel::onTitleChange,
        onConfirm = {
            viewModel.onConfirmClicked()
            navigator.popBackStack()
        },
        onDismissRequest = { navigator.popBackStack() }
    )
}