package com.ramcosta.destinations.sample.wear.tasks.presentation.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.destinations.sample.wear.core.viewmodel.viewModel

@Destination<RootGraph>(navArgs = StepScreenNavArgs::class)
@Composable
fun StepScreen(
    viewModel: StepDetailsViewModel = viewModel()
) {
    val step = viewModel.step.collectAsState().value

    if (step == null) {
        CircularProgressIndicator()
        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Completed")

                Checkbox(
                    checked = step.completed,
                    onCheckedChange = viewModel::onStepCheckedChange
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
