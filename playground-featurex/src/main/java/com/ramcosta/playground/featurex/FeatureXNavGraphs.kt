package com.ramcosta.playground.featurex

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.generated.destinations.FeatureXHomeDestination
import com.ramcosta.composedestinations.generated.navgraphs.FeatureXGraph
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.wrapper.DestinationWrapper

@NavGraph(
    navArgs = FeatureXNavArgs::class,
    default = true
)
internal annotation class FeatureXNavGraph(
    val start: Boolean = false
)

data class FeatureXNavArgs(
    val something: String
)

data class FeatureXHomeNavArgs(
    val something2: String
)

object FeatureXWrapper: DestinationWrapper {

    @Composable
    override fun <T> DestinationScope<T>.Wrap(screenContent: @Composable () -> Unit) {
        Column(Modifier.fillMaxSize()) {
            Text("FROM WRAPPER")
            screenContent()
        }
    }

}

@FeatureXNavGraph(start = true)
@Destination(
    navArgs = FeatureXHomeNavArgs::class,
    wrappers = [FeatureXWrapper::class]
)
@Composable
internal fun FeatureXHome(
    navBackStackEntry: NavBackStackEntry,
) {
    Column {
        Text("FeatureX Home screen args = ${FeatureXHomeDestination.argsFrom(navBackStackEntry)}")
        Text("FeatureX Graph args = ${runCatching { FeatureXGraph.argsFrom(navBackStackEntry) }.getOrElse { "Navigated directly to start destination, nav graph specific args are not available." }}")
    }
}

@NavGraph
internal annotation class FeatureYNavGraph(
    val start: Boolean = false
)

@FeatureYNavGraph(start = true)
@Destination
@Composable
internal fun FeatureYHome() {
    Text("FeatureY Home screen")
}