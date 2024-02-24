package com.ramcosta.playground.featurex

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.NoParent

@NavGraph<NoParent>(
    navArgs = FeatureXNavArgs::class,
)
internal annotation class FeatureXGraph

data class FeatureXNavArgs(
    val something: String
)