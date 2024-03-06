package com.ramcosta.playground.featurex

import com.ramcosta.composedestinations.annotation.ExternalModuleGraph
import com.ramcosta.composedestinations.annotation.NavGraph

@NavGraph<ExternalModuleGraph>(
    navArgs = FeatureXNavArgs::class,
)
internal annotation class FeatureXGraph

data class FeatureXNavArgs(
    val something: String
)
