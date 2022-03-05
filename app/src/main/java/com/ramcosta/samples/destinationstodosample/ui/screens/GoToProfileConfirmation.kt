package com.ramcosta.samples.destinationstodosample.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.samples.destinationstodosample.ui.screens.styles.AppDialog

@Destination(style = AppDialog::class)
@Composable
fun GoToProfileConfirmation(
    resultNavigator: ResultBackNavigator<Boolean>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Are you sure you want to go to Profile Screen?")
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                resultNavigator.navigateBack(result = true)
            }
        ) {
            Text(text = "Yes, why is this even a dialog?!")
        }
    }
}