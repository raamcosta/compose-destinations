package com.ramcosta.playground.featurex

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

@NavGraph
internal annotation class FeatureYNavGraph(
    val start: Boolean = false
)
