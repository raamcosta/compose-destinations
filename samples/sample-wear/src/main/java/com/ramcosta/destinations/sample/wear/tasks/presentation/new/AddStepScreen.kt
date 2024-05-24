package com.ramcosta.destinations.sample.wear.tasks.presentation.new

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.destinations.sample.wear.core.viewmodel.viewModel
import com.ramcosta.destinations.sample.wear.ui.composables.TitleConfirmDialog

data class AddStepDialogNavArgs(
    val taskId: Int
)

@Destination<RootGraph>(navArgs = AddStepDialogNavArgs::class)
@Composable
fun AddStepScreen(
    navigator: DestinationsNavigator,
    viewModel: AddStepViewModel = viewModel()
) {
    TitleConfirmDialog(
        type = "step", //use string resources in a real app ofc :)
        title = viewModel.title.collectAsState().value,
        onTitleChange = viewModel::onTitleChange,
        onConfirm = {
            viewModel.onConfirmClicked()
            navigator.popBackStack()
        },
        onDismissRequest = { navigator.popBackStack() }
    )
}
