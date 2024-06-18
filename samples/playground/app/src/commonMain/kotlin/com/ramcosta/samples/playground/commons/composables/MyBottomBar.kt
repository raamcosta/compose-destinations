package com.ramcosta.samples.playground.commons.composables

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.samples.playground.ui.screens.destinations.FeedDestination
import com.ramcosta.samples.playground.ui.screens.destinations.GreetingScreenDestination

enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    val icon: ImageVector,
    val label: String
) {
    Greeting(GreetingScreenDestination, Icons.Default.Home, "Greeting Screen"),
    Feed(FeedDestination, Icons.Default.Email, "Feed Screen"),
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
                    Icon(destination.icon, contentDescription = destination.label)
                },
                label = {
                    Text(destination.label)
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