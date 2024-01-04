package com.ramcosta.playground.featurex.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.featurex.destinations.FeatureXHomeDestination
import com.ramcosta.composedestinations.generated.featurex.destinations.InternalArgsScreenDestination
import com.ramcosta.composedestinations.generated.featurex.navgraphs.FeatureXGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.playground.featurex.FeatureXNavGraph
import com.ramcosta.playground.featurex.FeatureXWrapper

data class FeatureXHomeNavArgs(
    val something2: String
)

@FeatureXNavGraph(start = true)
@Destination(
    navArgs = FeatureXHomeNavArgs::class,
    wrappers = [FeatureXWrapper::class]
)
@Composable
internal fun FeatureXHome(
    destinationsNavigator: DestinationsNavigator,
    navBackStackEntry: NavBackStackEntry,
) {
    Column {
        Text("FeatureX Home screen args = ${FeatureXHomeDestination.argsFrom(navBackStackEntry)}")

        Text("FeatureX Graph args = ${runCatching { FeatureXGraph.argsFrom(navBackStackEntry) }.getOrElse { "Navigated directly to start destination, nav graph specific args are not available." }}")

        Button(
            onClick = {
                destinationsNavigator.navigate(
                    InternalArgsScreenDestination(arrayListOf(InternalSerializableArg("some internal arg")))
                )
            }
        ) {
            Text("Navigate to Internal screen")
        }
    }
}