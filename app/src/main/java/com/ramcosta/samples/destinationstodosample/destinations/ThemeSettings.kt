package com.ramcosta.samples.destinationstodosample.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.ThemeSettingsDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.samples.destinationstodosample.destinations.commons.Routes
import com.ramcosta.samples.destinationstodosample.destinations.commons.requireTitle

@Destination(
    route = Routes.THEME_SETTINGS,
    navGraph = Routes.SETTINGS_NAV_GRAPH,
    style = DestinationStyle.BottomSheet::class
)
@Composable
fun ColumnScope.ThemeSettings() {
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