package com.ramcosta.samples.playground.commons

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.animations.defaults.NoTransitions
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.annotation.IncludeDestination
import com.ramcosta.composedestinations.annotation.IncludeNavGraph
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.annotation.paramtypes.CodeGenVisibility
import com.ramcosta.composedestinations.generated.featurex.navgraphs.FeatureXGraph
import com.ramcosta.composedestinations.generated.featurey.destinations.FeatureYHomeDestination
import com.ramcosta.composedestinations.generated.featurey.navgraphs.FeatureYGraph
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import com.ramcosta.playground.core.WithDefaultValueArgs
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileGraph
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileSettingsGraph
import kotlin.reflect.KClass

/*
 TODO RACOSTA:
 - Enable multiple destinations in the same Composable - possibility to belong to multiple graphs as well
 - Runtime animations that could depend on some app logic or state - maybe with manual composable calls, also accept animations to be set, in
 which case we would call those instead of the ones from DestinationStyle.
*/

@RootNavGraph
@NavGraph(
    defaultTransitions = DefaultFadingTransitions::class
)
annotation class SettingsNavGraph(
    val start: Boolean = false
)

@RootNavGraph
@NavGraph(
    navArgs = ProfileNavGraph.NavArgs::class,
    deepLinks = [
        DeepLink(uriPattern = "https://destinationssample.com/$FULL_ROUTE_PLACEHOLDER")
    ],
    visibility = CodeGenVisibility.PUBLIC
)
annotation class ProfileNavGraph(
    val start: Boolean = false
) {
    data class NavArgs(
        val graphArg: String,
    )

    @IncludeDestination(FeatureYHomeDestination::class)
    @IncludeNavGraph(
        FeatureXGraph::class,
        deepLinks = [DeepLink(uriPattern = "https://cenas/$FULL_ROUTE_PLACEHOLDER"), DeepLink(uriPattern = "https://qweqwe/$FULL_ROUTE_PLACEHOLDER")],
        defaultTransitions = NoTransitions::class
    )
    @IncludeNavGraph(FeatureYGraph::class)
    companion object Includes
}

@NavHostGraph(
    visibility = CodeGenVisibility.INTERNAL
)
annotation class MyTopLevelNavGraph(
    val start: Boolean = false
)

@MyTopLevelNavGraph(start = true)
@Destination
@Composable
fun Asd() {
    Text("Asd")
}

@ProfileNavGraph(start = true)
@NavGraph(
    navArgs = ProfileSettingsNavGraph.NavArgs::class,
//    visibility = CodeGenVisibility.INTERNAL
)
annotation class ProfileSettingsNavGraph(
    val start: Boolean = false
) {
    data class NavArgs(
        val anotherGraphArg: String
    )
}

//@NavGraph(
//    defaultTransitions = NoTransitions::class,
//    navArgs = TestNavGraphNavArgs::class
//)
//annotation class TestNavGraph(
//    val start: Boolean = false
//)
//
//data class TestNavGraphNavArgs(
//    val testNavGraphArg: String
//)
//
//@TestNavGraph(start = true)
//@Destination
//internal fun TestTestScreen() {
//    Text("TEST")
//}


@Repeatable
@Destination(
    visibility = CodeGenVisibility.INTERNAL
)
annotation class InternalDestination(
    val route: String = Destination.COMPOSABLE_NAME,
    val navArgs: KClass<*> = Nothing::class,
    val deepLinks: Array<DeepLink> = [],
    val style: KClass<out DestinationStyle> = DestinationStyle.Default::class,
    val wrappers: Array<KClass<out DestinationWrapper>> = [],
)

//@ProfileNavGraph
//@InternalDestination
//annotation class ProfileDestination(
//    val navArgs: KClass<*> = Nothing::class
//)
//
//@ProfileSettingsNavGraph(start = true)
//@InternalDestination
//annotation class ProfileSettingsDestination(
//    val route: String = Destination.COMPOSABLE_NAME,
//    val navArgs: KClass<*> = Nothing::class
//)

@ProfileSettingsNavGraph
@Destination
annotation class ProfileSettingsDestination(
    val start: Boolean = false,
    val navArgs: KClass<*> = Nothing::class,
)

@ProfileSettingsDestination(start = true, navArgs = WithDefaultValueArgs::class)
@Destination(route = "settings/profile_settings_screen", navArgs = WithDefaultValueArgs::class)
@Composable
fun ProfileSettingsScreen(
//    vm: SettingsViewModel,
    args: WithDefaultValueArgs,
    navBackStackEntry: NavBackStackEntry
) = Column {
//    Text("VM toggle ON? ${vm.isToggleOn}")
    Text("$args")
//    Text("${navBackStackEntry.navArgs<ProfileNavGraph.NavArgs>()}")
    Text("${kotlin.runCatching { ProfileGraph.argsFrom(navBackStackEntry) }}")
    Text("${kotlin.runCatching { ProfileSettingsGraph.argsFrom(navBackStackEntry) } }")
}

//@ProfileSettingsNavGraph(start = true)
//@InternalDestination(navArgs = WithDefaultValueArgs::class)
//@Composable
//fun ProfileSettingsScreenZZZ(
////    vm: SettingsViewModel,
//    args: WithDefaultValueArgs,
//    navBackStackEntry: NavBackStackEntry
//) = Column {
////    Text("VM toggle ON? ${vm.isToggleOn}")
//    Text("$args")
////    Text("${navBackStackEntry.navArgs<ProfileNavGraph.NavArgs>()}")
//    Text("${kotlin.runCatching { ProfileGraph.argsFrom(navBackStackEntry) }}")
//    Text("${kotlin.runCatching { ProfileSettingsGraph.argsFrom(navBackStackEntry) } }")
//}
