package com.ramcosta.samples.destinationstodosample.destinations

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
import androidx.navigation.NavController
import com.ramcosta.composedestinations.SettingsDestination
import com.ramcosta.composedestinations.ThemeSettingsDestination
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigateTo
import com.ramcosta.samples.destinationstodosample.title

const val SETTINGS_NAV_GRAPH = "settings"
const val SETTINGS_DEEP_LINK_URI = "https://destinationssample.com/settings"

@Destination(
    route = "settings/main",
    start = true,
    navGraph = SETTINGS_NAV_GRAPH,
    deepLinks = [DeepLink(uriPattern = SETTINGS_DEEP_LINK_URI)]
)
@Composable
fun Settings(
    navController: NavController,
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
                onClick = { navController.navigateTo(ThemeSettingsDestination) }
            ) {
                Text(text = stringResource(id = ThemeSettingsDestination.title))
            }

            Text(
                text = stringResource(id = SettingsDestination.title)
            )
        }
    }
}