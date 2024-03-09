package com.ramcosta.composedestinations.testing

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DestinationsNavController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

/**
 * Creates a [DestinationsNavigator] good for testing.
 *
 * @param testNavController should be `TestNavHostController` from navigation testing artifact
 * @param isCurrentBackStackEntryResumed allows you to control behavior of `onlyIfResumed` parameter
 * on navigate calls. By default, uses the real response from [testNavController]'s current nav back stack entry.
 */
@Suppress("FunctionName")
fun TestDestinationsNavigator(
    testNavController: NavController,
    isCurrentBackStackEntryResumed: () -> Boolean = {
        testNavController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
    }
): DestinationsNavigator = DestinationsNavController(
    navController = testNavController,
    isCurrentBackStackEntryResumed = isCurrentBackStackEntryResumed
)