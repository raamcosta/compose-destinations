package com.ramcosta.samples.playground.commons

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.animations.defaults.NoTransitions
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalRoutes
import com.ramcosta.composedestinations.annotation.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.annotation.paramtypes.CodeGenVisibility
import com.ramcosta.composedestinations.generated.navgraphs.FeatureXGraph
import com.ramcosta.composedestinations.generated.navgraphs.FeatureYGraph
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import com.ramcosta.playground.core.WithDefaultValueArgs
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileGraph
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileSettingsGraph
import kotlin.reflect.KClass

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
    externalRoutes = ExternalRoutes(
        nestedNavGraphs = [
            FeatureXGraph::class,
            FeatureYGraph::class
        ],
//        startRoute = FeatureXGraph::class,
    ),
    visibility = CodeGenVisibility.PUBLIC
)
annotation class ProfileNavGraph(
    val start: Boolean = false
) {
    data class NavArgs(
        val graphArg: String,
    )
}

@NavHostGraph(
    defaultTransitions = NoTransitions::class,
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


@Destination
annotation class InternalDestination(
    val navArgs: KClass<*> = Nothing::class,
    val deepLinks: Array<DeepLink> = [],
    val style: KClass<out DestinationStyle> = DestinationStyle.Default::class,
    val wrappers: Array<KClass<out DestinationWrapper>> = [],
    val visibility: CodeGenVisibility = CodeGenVisibility.INTERNAL
)

@ProfileSettingsNavGraph(start = true)
@InternalDestination(
    navArgs = WithDefaultValueArgs::class
)
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
