package com.ramcosta.playground.featurey.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.dokar.sonner.rememberToasterState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.featurey.destinations.FeatureYInternalArgsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.playground.featurey.FeatureYGraph

@kotlinx.serialization.Serializable
data class InternalBackResult(val value: Boolean)

@Destination<FeatureYGraph>(start = true)
@Composable
internal fun FeatureYHome(
    navigator: DestinationsNavigator,
    backNavigator: ResultBackNavigator<InternalBackResult>,
    internalResult: ResultRecipient<FeatureYInternalArgsScreenDestination, Boolean?>
) = Column {

    val toaster = rememberToasterState()
    internalResult.onNavResult {
        toaster.show(it)
        when (it) {
            is NavResult.Canceled -> Unit
            is NavResult.Value -> if (it.value != null) {
                backNavigator.navigateBack(result = InternalBackResult(it.value!!))
            }
        }
    }

    Text("FeatureY Home screen")

    Button(
        onClick = { navigator.navigate(FeatureYInternalArgsScreenDestination(arrayListOf())) }
    ) {
        Text("Go to internal destination")
    }
}