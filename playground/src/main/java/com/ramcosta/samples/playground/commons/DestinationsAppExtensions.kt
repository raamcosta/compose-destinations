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
import com.ramcosta.samples.playground.ui.screens.NavGraph
import com.ramcosta.samples.playground.ui.screens.NavGraphs
import com.ramcosta.samples.playground.ui.screens.destinations.*
import java.io.InvalidObjectException

@Composable
fun DirectionDestination.DrawerContent(
    isSelected: Boolean,
    onDestinationClick: (DirectionDestination) -> Unit
) {
    when (this) {
        FeedDestination,
        GreetingScreenDestination -> {
            Text(
                text = stringResource(id = requireTitle),
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        onDestinationClick(this)
                    },
                fontWeight = if (isSelected) FontWeight.Bold else null
            )
        }
        GoToProfileConfirmationDestination,
        SettingsScreenDestination,
        ThemeSettingsDestination -> Unit
    }
}

@get:StringRes
val Destination.requireTitle
    get(): Int {
        return title ?: throw RuntimeException("Destination $this, doesn't contain title")
    }

@get:StringRes
val Destination.title
    get(): Int? {
        return when (this) {
            GreetingScreenDestination -> R.string.greeting_screen
            ProfileScreenDestination -> R.string.profile_screen
            SettingsScreenDestination -> R.string.settings_screen
            FeedDestination -> R.string.feed_screen
            ThemeSettingsDestination -> R.string.theme_settings_screen
            GoToProfileConfirmationDestination -> null
            TestScreenDestination -> null
            TestScreen2Destination -> null
        }
    }

val DestinationSpec<*>.parent: NavGraph
    get() {
        val parent =  NavGraphs.all.find {
            it.destinations.contains(this)
        }

        return parent ?: throw InvalidObjectException("The calling object should always have a parent!")
    }