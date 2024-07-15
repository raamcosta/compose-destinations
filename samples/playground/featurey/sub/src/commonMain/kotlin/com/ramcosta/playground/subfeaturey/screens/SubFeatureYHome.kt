package com.ramcosta.playground.subfeaturey.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.dokar.sonner.ToasterState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.subfeaturey.destinations.SubFeatureYInternalArgsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.playground.subfeaturey.SubFeatureYGraph

@Destination<SubFeatureYGraph>(start = true)
@Composable
internal fun SubFeatureYHome(
    toaster: ToasterState,
    navigator: DestinationsNavigator,
    backNavigator: ResultBackNavigator<Boolean>,
    internalResult: ResultRecipient<SubFeatureYInternalArgsScreenDestination, Boolean?>
) = Column {
    internalResult.onNavResult {
        toaster.show( "$it")
        when (it) {
            is NavResult.Canceled -> Unit
            is NavResult.Value -> if (it.value != null) {
                backNavigator.navigateBack(result = it.value!!)
            }
        }
    }

    Text("FeatureY Home screen")

    Button(
        onClick = { navigator.navigate(SubFeatureYInternalArgsScreenDestination(arrayListOf())) }
    ) {
        Text("Go to internal destination")
    }
}