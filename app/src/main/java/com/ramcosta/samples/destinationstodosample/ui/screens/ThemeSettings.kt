package com.ramcosta.samples.destinationstodosample.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.samples.destinationstodosample.commons.Routes
import com.ramcosta.samples.destinationstodosample.commons.requireTitle
import com.ramcosta.samples.destinationstodosample.ui.screens.destinations.ThemeSettingsDestination

@Destination(
    route = Routes.THEME_SETTINGS,
    navGraph = Routes.SETTINGS_NAV_GRAPH,
)
@Composable
fun ThemeSettings() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Yellow)
    ) {
        Text(
            text = stringResource(id = ThemeSettingsDestination.requireTitle),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}