package com.ramcosta.samples.destinationstodosample.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.GoToProfileConfirmationDestination
import com.ramcosta.composedestinations.ProfileDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.samples.destinationstodosample.destinations.styles.AppDialog

@Destination(style = AppDialog::class)
@Composable
fun GoToProfileConfirmation(
    navigator: DestinationsNavigator
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Are you sure you want to go to Profile Screen?")
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                navigator.navigate(ProfileDestination.withArgs(id2 = 3)) {
                    popUpTo(GoToProfileConfirmationDestination.route) {
                        inclusive = true
                    }
                }
            }
        ) {
            Text(text = "Yes, why is this even a dialog?!")
        }
    }
}