package com.ramcosta.destinations.sample.tasks.presentation.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.destinations.sample.core.viewmodel.viewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Completed:")

            Checkbox(
                checked = step.completed,
                onCheckedChange = viewModel::onStepCheckedChange
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
