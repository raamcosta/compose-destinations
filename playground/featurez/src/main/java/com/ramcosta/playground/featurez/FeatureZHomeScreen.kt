package com.ramcosta.playground.featurez

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.ExternalModuleGraph
import com.ramcosta.composedestinations.generated.featurez.destinations.FeatureZSecondScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination<ExternalModuleGraph>
@Composable
internal fun FeatureZHome(
    navigator: DestinationsNavigator,
) = Column {
    Text("FeatureZ Home screen")

    Button(
        onClick = { navigator.navigate(FeatureZSecondScreenDestination) }
    ) {
        Text("Go to internal destination")
    }
}