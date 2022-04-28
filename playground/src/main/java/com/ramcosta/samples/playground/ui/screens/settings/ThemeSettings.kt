package com.ramcosta.samples.playground.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.samples.playground.commons.SettingsNavGraph
import com.ramcosta.samples.playground.commons.requireTitle
import com.ramcosta.samples.playground.ui.screens.destinations.ThemeSettingsDestination
import com.ramcosta.samples.playground.ui.screens.profile.SerializableExampleWithNavTypeSerializer

@SettingsNavGraph
@Destination(style = DestinationStyle.BottomSheet::class)
@Composable
fun ColumnScope.ThemeSettings(
    viewModel: SettingsViewModel,
    resultNavigator: ResultBackNavigator<SerializableExampleWithNavTypeSerializer>
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Yellow)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Switch(checked = viewModel.isToggleOn, onCheckedChange = { viewModel.toggle() })

            Text(
                text = stringResource(id = ThemeSettingsDestination.requireTitle),
            )

            Button(
                onClick = {
                    resultNavigator.navigateBack(
                        result = SerializableExampleWithNavTypeSerializer("RESULT!!", "THING2")
                    )
                }
            ) {
                Text("Go back with result")
            }
        }
    }
}