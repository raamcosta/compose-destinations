package com.ramcosta.samples.playground.commons

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.animations.defaults.NoTransitions
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalDestination
import com.ramcosta.composedestinations.annotation.ExternalNavGraph
import com.ramcosta.composedestinations.annotation.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.annotation.paramtypes.CodeGenVisibility
import com.ramcosta.composedestinations.generated.featurex.navgraphs.FeatureXGraph
import com.ramcosta.composedestinations.generated.featurey.destinations.PublicFeatureYSideScreenDestination
import com.ramcosta.composedestinations.generated.featurey.navgraphs.FeatureYGraph
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import com.ramcosta.playground.core.WithDefaultValueArgs
import com.ramcosta.samples.playground.ui.screens.destinations.ProfileSettingsProfileSettingsScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.RootProfileSettingsScreenDestination
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileGraph
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileSettingsGraph
import kotlin.reflect.KClass

/*
 TODO RACOSTA:
 - Enable multiple destinations in the same Composable - possibility to belong to multiple graphs as well
 - Runtime animations that could depend on some app logic or state - maybe with manual composable calls, also accept animations to be set, in
 which case we would call those instead of the ones from DestinationStyle.
*/

@NavGraph<RootNavGraph>(
    defaultTransitions = DefaultFadingTransitions::class
)
annotation class SettingsNavGraph

@NavGraph<RootNavGraph>(
    navArgs = ProfileNavGraph.NavArgs::class,
    deepLinks = [
        DeepLink(uriPattern = "https://destinationssample.com/$FULL_ROUTE_PLACEHOLDER")
    ],
    visibility = CodeGenVisibility.PUBLIC
)
annotation class ProfileNavGraph {
    data class NavArgs(
        val graphArg: String,
    )

    @ExternalDestination<PublicFeatureYSideScreenDestination>
    @ExternalNavGraph<FeatureXGraph>(
        deepLinks = [
            DeepLink(uriPattern = "https://cenas/$FULL_ROUTE_PLACEHOLDER"),
            DeepLink(uriPattern = "https://qweqwe/$FULL_ROUTE_PLACEHOLDER")
        ],
        defaultTransitions = NoTransitions::class
    )
    @ExternalNavGraph<FeatureYGraph>
    companion object Includes
}

@NavHostGraph(
    visibility = CodeGenVisibility.INTERNAL
)
annotation class MyTopLevelNavGraph

@Destination<MyTopLevelNavGraph>(start = true)
@Composable
fun Asd() {
    Text("Asd")
}

@NavGraph<ProfileNavGraph>(
    start = true,
    navArgs = ProfileSettingsNavGraph.NavArgs::class,
)
annotation class ProfileSettingsNavGraph {
    data class NavArgs(
        val anotherGraphArg: String
    )
}

@Repeatable
@Destination<RootNavGraph>(
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

@InternalDestination<SettingsNavGraph>
@Destination<RootNavGraph>
@Composable
fun StatsScreen() {
    Text("StatsScreen")
}

@Destination<ProfileSettingsNavGraph>(start = true, navArgs = WithDefaultValueArgs::class)
@Destination<RootNavGraph>(navArgs = WithDefaultValueArgs::class)
@Composable
fun ProfileSettingsScreen(
    args: WithDefaultValueArgs,
    navBackStackEntry: NavBackStackEntry
) = Column(verticalArrangement = Arrangement.Absolute.spacedBy(2.dp)) {
    Text("$args")
    Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).border(1.dp, Color.Black))
    Text("${kotlin.runCatching { ProfileGraph.argsFrom(navBackStackEntry) }}")
    Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).border(1.dp, Color.Black))
    Text("${kotlin.runCatching { ProfileSettingsGraph.argsFrom(navBackStackEntry) } }")
    Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).border(1.dp, Color.Black))
    Text("${kotlin.runCatching { RootProfileSettingsScreenDestination.argsFrom(navBackStackEntry) } }")
    Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).border(1.dp, Color.Black))
    Text("${kotlin.runCatching { ProfileSettingsProfileSettingsScreenDestination.argsFrom(navBackStackEntry) } }")
}
