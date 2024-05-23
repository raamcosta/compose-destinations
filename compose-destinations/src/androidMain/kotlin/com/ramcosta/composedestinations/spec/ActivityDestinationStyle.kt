package com.ramcosta.composedestinations.spec

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.activity
import com.ramcosta.composedestinations.annotation.internal.InternalDestinationsApi
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.manualcomposablecalls.allDeepLinks
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder

@InternalDestinationsApi
object ActivityDestinationStyle: DestinationStyle() {
    override fun <T> NavGraphBuilder.addComposable(
        destination: TypedDestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: ManualComposableCalls
    ) {
        destination as ActivityDestinationSpec<T>

        addComposable(destination, manualComposableCalls)
    }

    internal fun <T> NavGraphBuilder.addComposable(
        destination: ActivityDestinationSpec<T>,
        manualComposableCalls: ManualComposableCalls? = null
    ) {
        activity(destination.route) {
            targetPackage = destination.targetPackage
            activityClass = destination.activityClass?.kotlin
            action = destination.action
            data = destination.data
            dataPattern = destination.dataPattern

            destination.allDeepLinks(manualComposableCalls).forEach { deepLink ->
                deepLink {
                    action = deepLink.action
                    uriPattern = deepLink.uriPattern
                    mimeType = deepLink.mimeType
                }
            }

            destination.arguments.forEach { navArg ->
                argument(navArg.name) {
                    if (navArg.argument.isDefaultValuePresent) {
                        defaultValue = navArg.argument.defaultValue
                    }
                    type = navArg.argument.type
                    nullable = navArg.argument.isNullable
                }
            }
        }
    }

}