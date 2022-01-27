package com.ramcosta.samples.destinationstodosample.ui.screens

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
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.ramcosta.composedestinations.result.EmptyResultRecipient
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.samples.destinationstodosample.commons.Routes
import com.ramcosta.samples.destinationstodosample.commons.requireTitle
import com.ramcosta.samples.destinationstodosample.ui.screens.destinations.SettingsDestination
import com.ramcosta.samples.destinationstodosample.ui.screens.destinations.ThemeSettingsDestination
import com.ramcosta.samples.destinationstodosample.ui.screens.profile.SerializableExample
import com.ramcosta.samples.destinationstodosample.ui.screens.styles.SettingsTransitions

const val SETTINGS_DEEP_LINK_URI = "https://destinationssample.com/settings"

@OptIn(ExperimentalAnimationApi::class)
@Destination(
    route = Routes.MAIN_SETTINGS,
    start = true,
    navGraph = Routes.SETTINGS_NAV_GRAPH,
    deepLinks = [DeepLink(uriPattern = SETTINGS_DEEP_LINK_URI)],
    style = SettingsTransitions::class
)
@Composable
fun Settings(
    navigator: DestinationsNavigator,
    themeSettingsResultRecipient: ResultRecipient<ThemeSettingsDestination, SerializableExample>
) {
    themeSettingsResultRecipient.onResult {
        println("result = $it")
    }

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
                Text(text = stringResource(id = ThemeSettingsDestination.requireTitle))
            }

            Text(
                text = stringResource(id = SettingsDestination.requireTitle)
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
    Settings(
        EmptyDestinationsNavigator,
        EmptyResultRecipient()
    )
}