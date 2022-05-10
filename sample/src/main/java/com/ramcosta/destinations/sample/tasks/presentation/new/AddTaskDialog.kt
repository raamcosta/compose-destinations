package com.ramcosta.destinations.sample.tasks.presentation.new

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.destinations.sample.core.viewmodel.viewModel
import com.ramcosta.destinations.sample.ui.composables.TitleConfirmDialog

@Destination(style = DestinationStyle.Dialog::class)
@Composable
fun AddTaskDialog(
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
        }
    )
}