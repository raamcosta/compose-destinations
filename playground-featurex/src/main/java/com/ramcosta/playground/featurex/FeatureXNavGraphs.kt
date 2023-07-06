package com.ramcosta.playground.featurex

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph

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

@FeatureXNavGraph(start = true)
@Destination(
    navArgs = FeatureXHomeNavArgs::class
)
@Composable
internal fun FeatureXHome() {
    Text("FeatureX Home screen")
}

@NavGraph
internal annotation class FeatureYNavGraph(
    val start: Boolean = false
)

@FeatureYNavGraph(start = true)
@Destination()
@Composable
internal fun FeatureYHome() {
    Text("FeatureY Home screen")
}