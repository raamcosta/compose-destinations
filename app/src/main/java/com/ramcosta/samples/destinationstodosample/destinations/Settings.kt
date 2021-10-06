package com.ramcosta.samples.destinationstodosample.destinations

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.*
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.ramcosta.samples.destinationstodosample.destinations.transitions.SettingsTransitions
import com.ramcosta.samples.destinationstodosample.title

const val SETTINGS_ROUTE = "settings/main"
const val SETTINGS_NAV_GRAPH = "settings"
const val SETTINGS_DEEP_LINK_URI = "https://destinationssample.com/settings"

@OptIn(ExperimentalAnimationApi::class)
@Destination(
    route = SETTINGS_ROUTE,
    start = true,
    navGraph = SETTINGS_NAV_GRAPH,
    deepLinks = [DeepLink(uriPattern = SETTINGS_DEEP_LINK_URI)],
    transitions = SettingsTransitions::class
)
@Composable
fun Settings(
    navigator: DestinationsNavigator,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Magenta)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Button(
                onClick = { navigator.navigate(ThemeSettingsDestination) }
            ) {
                Text(text = stringResource(id = ThemeSettingsDestination.title))
            }

            Text(
                text = stringResource(id = SettingsDestination.title)
            )
        }
    }
}

/**
 * As an example of a preview
 */
//@Preview
@Composable
fun SettingsPreview() {
    Settings(EmptyDestinationsNavigator)
}