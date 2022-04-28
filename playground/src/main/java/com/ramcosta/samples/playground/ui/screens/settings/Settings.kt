package com.ramcosta.samples.playground.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.ramcosta.composedestinations.result.EmptyResultRecipient
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.samples.playground.commons.SettingsNavGraph
import com.ramcosta.samples.playground.commons.requireTitle
import com.ramcosta.samples.playground.ui.screens.destinations.SettingsScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.ThemeSettingsDestination
import com.ramcosta.samples.playground.ui.screens.profile.SerializableExampleWithNavTypeSerializer
import com.ramcosta.samples.playground.ui.screens.styles.SettingsTransitions

const val SETTINGS_DEEP_LINK_URI = "https://destinationssample.com/settings"

@OptIn(ExperimentalAnimationApi::class)
@SettingsNavGraph(start = true)
@Destination(
    deepLinks = [DeepLink(uriPattern = SETTINGS_DEEP_LINK_URI)],
    style = SettingsTransitions::class
)
@Composable
fun SettingsScreen(
    navigator: DestinationsNavigator,
    viewModel: SettingsViewModel,
    themeSettingsResultRecipient: ResultRecipient<ThemeSettingsDestination, SerializableExampleWithNavTypeSerializer>
) {
    val context = LocalContext.current
    themeSettingsResultRecipient.onNavResult {
        println("result = $it")
        Toast.makeText(context, "confirmed? = $it", Toast.LENGTH_LONG).show()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Magenta)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Switch(checked = viewModel.isToggleOn, onCheckedChange = { viewModel.toggle() })

            Button(
                onClick = { navigator.navigate(ThemeSettingsDestination) }
            ) {
                Text(text = stringResource(id = ThemeSettingsDestination.requireTitle))
            }

            Text(
                text = stringResource(id = SettingsScreenDestination.requireTitle)
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
    SettingsScreen(
        EmptyDestinationsNavigator,
        SettingsViewModel(),
        EmptyResultRecipient()
    )
}