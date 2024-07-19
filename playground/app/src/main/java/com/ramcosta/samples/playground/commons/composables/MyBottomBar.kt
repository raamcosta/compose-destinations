package com.ramcosta.samples.playground.commons.composables

import androidx.annotation.StringRes
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.samples.playground.R
import com.ramcosta.samples.playground.commons.defaultRootStartArgs
import com.ramcosta.samples.playground.ui.screens.destinations.FeedDestination
import com.ramcosta.samples.playground.ui.screens.destinations.GreetingScreenDestination

enum class BottomBarDestination(
    val direction: Direction,
    val icon: ImageVector,
    @StringRes val label: Int
) {
    Greeting(GreetingScreenDestination(defaultRootStartArgs), Icons.Default.Home, R.string.greeting_screen),
    Feed(FeedDestination, Icons.Default.Email, R.string.feed_screen),
}

@Composable
fun BottomBar(
    currentDestination: DestinationSpec,
    onBottomBarItemClick: (Direction) -> Unit
) {
    BottomNavigation {
        BottomBarDestination.values().forEach { destination ->
            BottomNavigationItem(
                icon = {
                    Icon(destination.icon, contentDescription = stringResource(destination.label))
                },
                label = {
                    Text(stringResource(destination.label))
                },
                alwaysShowLabel = false,
                selected = currentDestination == destination.direction,
                onClick = {
                    onBottomBarItemClick(destination.direction)
                },
            )
        }
    }
}