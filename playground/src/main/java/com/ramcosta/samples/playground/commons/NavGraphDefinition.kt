package com.ramcosta.samples.playground.commons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.animations.defaults.NoTransitions
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalDestination
import com.ramcosta.composedestinations.annotation.ExternalNavGraph
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.annotation.parameters.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.generated.featurex.navgraphs.FeatureXNavGraph
import com.ramcosta.composedestinations.generated.featurey.destinations.PublicFeatureYSideScreenDestination
import com.ramcosta.composedestinations.generated.featurey.navgraphs.FeatureYNavGraph
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import com.ramcosta.playground.core.WithDefaultValueArgs
import com.ramcosta.samples.playground.ui.screens.destinations.ProfileSettingsProfileSettingsScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.RootProfileSettingsScreenDestination
import com.ramcosta.samples.playground.ui.screens.navGraphArgs
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileNavGraphArgs
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileSettingsNavGraphArgs
import kotlin.reflect.KClass

/*
 TODO RACOSTA:
 - Enable multiple destinations in the same Composable - possibility to belong to multiple graphs as well
 - Runtime animations that could depend on some app logic or state - maybe with manual composable calls, also accept animations to be set, in
 which case we would call those instead of the ones from DestinationStyle.
*/

@NavGraph<RootGraph>(
    defaultTransitions = DefaultFadingTransitions::class
)
annotation class SettingsGraph

@NavGraph<RootGraph>(
    navArgs = ProfileGraph.NavArgs::class,
    deepLinks = [
        DeepLink(uriPattern = "https://destinationssample.com/$FULL_ROUTE_PLACEHOLDER")
    ],
    visibility = CodeGenVisibility.PUBLIC
)
annotation class ProfileGraph {
    data class NavArgs(
        val graphArg: String,
    )

    @ExternalDestination<PublicFeatureYSideScreenDestination>
    @ExternalNavGraph<FeatureXNavGraph>(
        deepLinks = [
            DeepLink(uriPattern = "https://cenas/$FULL_ROUTE_PLACEHOLDER"),
            DeepLink(uriPattern = "https://qweqwe/$FULL_ROUTE_PLACEHOLDER")
        ],
        defaultTransitions = NoTransitions::class
    )
    @ExternalNavGraph<FeatureYNavGraph>
    companion object Includes
}

@NavHostGraph(
    visibility = CodeGenVisibility.INTERNAL
)
annotation class MyTopLevelGraph

@Destination<MyTopLevelGraph>(start = true)
@Composable
fun Asd() {
    Text("Asd")
}

@NavGraph<ProfileGraph>(
    start = true,
    navArgs = ProfileSettingsGraph.NavArgs::class,
)
annotation class ProfileSettingsGraph {
    data class NavArgs(
        val anotherGraphArg: String
    )
}

@Repeatable
@Destination<RootGraph>(
    visibility = CodeGenVisibility.INTERNAL
)
annotation class InternalDestination<T: Annotation>(
    val route: String = Destination.COMPOSABLE_NAME,
    val start: Boolean = false,
    val navArgs: KClass<*> = Nothing::class,
    val deepLinks: Array<DeepLink> = [],
    val style: KClass<out DestinationStyle> = DestinationStyle.Default::class,
    val wrappers: Array<KClass<out DestinationWrapper>> = [],
)

@InternalDestination<SettingsGraph>
@Destination<RootGraph>
@Composable
fun StatsScreen() {
    Text("StatsScreen")
}

@Destination<ProfileSettingsGraph>(start = true, navArgs = WithDefaultValueArgs::class)
@Destination<RootGraph>(navArgs = WithDefaultValueArgs::class)
@Composable
fun ProfileSettingsScreen(
    args: WithDefaultValueArgs,
    navBackStackEntry: NavBackStackEntry
) = Column(verticalArrangement = Arrangement.Absolute.spacedBy(6.dp)) {
    Text("$args")
    Divider(modifier = Modifier.fillMaxWidth())
    Text("${navBackStackEntry.navGraphArgs<ProfileNavGraphArgs>()}")
    Divider(modifier = Modifier.fillMaxWidth())
    Text("${navBackStackEntry.navGraphArgs<ProfileSettingsNavGraphArgs>()}")
    Divider(modifier = Modifier.fillMaxWidth())
    Text("${RootProfileSettingsScreenDestination.argsFrom(navBackStackEntry)}")
    Divider(modifier = Modifier.fillMaxWidth())
    Text("${ProfileSettingsProfileSettingsScreenDestination.argsFrom(navBackStackEntry)}")
}
