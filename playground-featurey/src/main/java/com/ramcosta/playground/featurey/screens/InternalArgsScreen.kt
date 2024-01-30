package com.ramcosta.playground.featurey.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.paramtypes.CodeGenVisibility
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.playground.featurey.FeatureYNavGraph
import kotlinx.serialization.Serializable


internal data class InternalArgs(
//    val internalClass: InternalSerializableArg,
    val arrayOfInternals: ArrayList<InternalSerializableArg>
)

@Serializable
internal data class InternalSerializableArg(
    val someValue: String
)

@Destination<FeatureYNavGraph>(
    navArgs = InternalArgs::class,
    visibility = CodeGenVisibility.INTERNAL
)
@Composable
internal fun FeatureYInternalArgsScreen(
    navArgs: InternalArgs,
    resultBackNavigator: ResultBackNavigator<Boolean?>
) = Column {
    var switchState by remember { mutableStateOf(false) }
    Text("Internal args screen $navArgs")

    Switch(
        checked = switchState,
        onCheckedChange = {
            switchState = it
        }
    )

    Button(
        onClick = { resultBackNavigator.navigateBack(result = switchState) }
    ) {
        Text("Go back with result!")
    }
}