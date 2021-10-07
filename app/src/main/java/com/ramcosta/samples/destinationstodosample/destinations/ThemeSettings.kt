package com.ramcosta.samples.destinationstodosample.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.DestinationStyle
import com.ramcosta.composedestinations.ThemeSettingsDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.samples.destinationstodosample.title

@Destination(
    route = "settings/theme",
    navGraph = SETTINGS_NAV_GRAPH,
    style = DestinationStyle.BottomSheet::class
)
@Composable
fun ThemeSettings() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Yellow)
    ) {
        Text(
            text = stringResource(id = ThemeSettingsDestination.title),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}