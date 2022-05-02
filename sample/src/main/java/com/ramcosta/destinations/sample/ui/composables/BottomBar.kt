package com.ramcosta.destinations.sample.ui.composables

import androidx.annotation.StringRes
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.navigateTo
import com.ramcosta.destinations.sample.R
import com.ramcosta.destinations.sample.destinations.*

@Composable
fun BottomBar(
    currentDestination: Destination,
    navController: NavHostController
) {
    BottomNavigation {
        BottomBarItem.values().forEach { destination ->
            BottomNavigationItem(
                selected = currentDestination == destination.direction,
                onClick = {
                    navController.navigateTo(destination.direction) {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        destination.icon,
                        contentDescription = stringResource(destination.label)
                    )
                },
                label = { Text(stringResource(destination.label)) },
            )
        }
    }
}

enum class BottomBarItem(
    val direction: DirectionDestination,
    val icon: ImageVector,
    @StringRes val label: Int
) {
    TaskList(TaskListScreenDestination, Icons.Default.Home, R.string.task_list_screen),
    Account(AccountScreenDestination, Icons.Default.Email, R.string.account_screen),
    Settings(SettingsScreenDestination, Icons.Default.Settings, R.string.settings_screen)
}