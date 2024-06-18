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
import com.ramcosta.composedestinations.annotation.ExternalModuleDestinations
import com.ramcosta.composedestinations.annotation.ExternalNavGraph
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.OverrideDestination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.annotation.parameters.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.bottomsheet.spec.DestinationStyleBottomSheet
import com.ramcosta.composedestinations.generated.featurex.navgraphs.FeatureXNavGraph
import com.ramcosta.composedestinations.generated.featurey.destinations.PublicFeatureYSideScreenDestination
import com.ramcosta.composedestinations.generated.featurey.navgraphs.FeatureYNavGraph
import com.ramcosta.composedestinations.generated.featurez.FeatureZModuleDestinations
import com.ramcosta.composedestinations.generated.featurez.destinations.FeatureZHomeDestination
import com.ramcosta.composedestinations.generated.featurez.destinations.FeatureZSecondScreenDestination
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import com.ramcosta.playground.core.ArgsFromAnotherModule
import com.ramcosta.samples.playground.ui.screens.destinations.ProfileSettingsProfileSettingsScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.RootProfileSettingsScreenDestination
import com.ramcosta.samples.playground.ui.screens.navGraphArgs
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileNavGraphArgs
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileSettingsNavGraphArgs
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@NavGraph<RootGraph>(
    defaultTransitions = DefaultFadingTransitions::class
)
annotation class SettingsGraph

@Serializable
data class GraphArgsTest(
    val cenas: String,
    val id: Int
)

@NavGraph<RootGraph>(
    navArgs = ProfileGraph.NavArgs::class,
    deepLinks = [
        DeepLink(uriPattern = "https://destinationssample.com/$FULL_ROUTE_PLACEHOLDER")
    ],
    visibility = CodeGenVisibility.PUBLIC
)
annotation class ProfileGraph {
    data class NavArgs(
        val graphArg: GraphArgsTest? = null,
    )

    @ExternalModuleDestinations<FeatureZModuleDestinations>(
        [
            OverrideDestination(
                destination = FeatureZHomeDestination::class,
                with = ExternalDestination(style = DestinationStyleBottomSheet::class),
            ),
            OverrideDestination(
                destination = FeatureZSecondScreenDestination::class,
                with = ExternalDestination(style = DestinationStyle.Dialog::class),
            ),
        ]
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
@Destination<Nothing>(
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

@Destination<ProfileSettingsGraph>(start = true, navArgs = ArgsFromAnotherModule::class)
@Destination<RootGraph>(navArgs = ArgsFromAnotherModule::class)
@Composable
fun ProfileSettingsScreen(
    args: ArgsFromAnotherModule,
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
