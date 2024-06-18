package com.ramcosta.samples.playground.commons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.spec.DestinationSpec
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
        text = title ?: this.toString(),
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                onDestinationClick(this)
            },
        fontWeight = if (isSelected) FontWeight.Bold else null
    )
}

// TODO Don't do this, since we no longer create sealed Destination, it doesn't work very well
val DestinationSpec.requireTitle
    get(): String {
        return title ?: throw RuntimeException("Destination $this, doesn't contain title")
    }

val DestinationSpec.title
    get(): String? {
        return when (this) {
            GreetingScreenDestination -> "Greeting Screen"
            ProfileScreenDestination -> "Profile Screen"
            SettingsScreenDestination -> "Settings Screen"
            FeedDestination -> "Feed Screen"
            ThemeSettingsDestination -> "Theme Settings Screen"
            else -> null
        }
    }