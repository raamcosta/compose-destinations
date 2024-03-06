package com.ramcosta.samples.playground.commons

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.samples.playground.R
import com.ramcosta.samples.playground.ui.screens.destinations.FeedDestination
import com.ramcosta.samples.playground.ui.screens.destinations.GreetingScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.ProfileScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.SettingsScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.ThemeSettingsDestination

@Composable
fun DestinationSpec.DrawerContent(
    isSelected: Boolean,
    onDestinationClick: (DestinationSpec) -> Unit
) {
    Text(
        text = title?.let { stringResource(id = it) } ?: this.toString(),
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                onDestinationClick(this)
            },
        fontWeight = if (isSelected) FontWeight.Bold else null
    )
}

// TODO Don't do this, since we no longer create sealed Destination, it doesn't work very well
@get:StringRes
val DestinationSpec.requireTitle
    get(): Int {
        return title ?: throw RuntimeException("Destination $this, doesn't contain title")
    }

@get:StringRes
val DestinationSpec.title
    get(): Int? {
        return when (this) {
            GreetingScreenDestination -> R.string.greeting_screen
            ProfileScreenDestination -> R.string.profile_screen
            SettingsScreenDestination -> R.string.settings_screen
            FeedDestination -> R.string.feed_screen
            ThemeSettingsDestination -> R.string.theme_settings_screen
            else -> null
        }
    }