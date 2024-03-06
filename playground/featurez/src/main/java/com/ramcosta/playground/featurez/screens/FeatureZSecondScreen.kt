package com.ramcosta.playground.featurez.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalModuleGraph

@Destination<ExternalModuleGraph>
@Composable
internal fun FeatureZSecondScreen() = Column {
    Text("FeatureZSecondScreen")
}