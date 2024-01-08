package com.ramcosta.playground.featurey.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.playground.featurey.FeatureYNavGraph

@FeatureYNavGraph(start = true)
@Destination
@Composable
internal fun FeatureYHome() {
    Text("FeatureY Home screen")
}