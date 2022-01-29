package com.ramcosta.samples.destinationstodosample.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.samples.destinationstodosample.commons.Routes
import com.ramcosta.samples.destinationstodosample.commons.requireTitle
import com.ramcosta.samples.destinationstodosample.ui.screens.destinations.ThemeSettingsDestination
import com.ramcosta.samples.destinationstodosample.ui.screens.profile.SerializableExample

@Destination(
    route = Routes.THEME_SETTINGS,
    navGraph = Routes.SETTINGS_NAV_GRAPH,
    style = DestinationStyle.BottomSheet::class
)
@Composable
fun ColumnScope.ThemeSettings(
    resultNavigator: ResultBackNavigator<SerializableExample>
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Yellow)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = ThemeSettingsDestination.requireTitle),
            )

            Button(
                onClick = {
                    resultNavigator.navigateBack(
                        result = SerializableExample("RESULT!!", "THING2")
                    )
                }
            ) {
                Text("Go back with result")
            }
        }
    }
}