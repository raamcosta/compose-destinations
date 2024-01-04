package com.ramcosta.playground.featurex.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.playground.featurex.FeatureYNavGraph

@FeatureYNavGraph(start = true)
@Destination
@Composable
internal fun FeatureYHome() {
    Text("FeatureY Home screen")
}