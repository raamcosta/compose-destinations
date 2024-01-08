package com.ramcosta.playground.featurey.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.paramtypes.CodeGenVisibility
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

@FeatureYNavGraph
@Destination(
    navArgs = InternalArgs::class,
    visibility = CodeGenVisibility.INTERNAL
)
@Composable
internal fun InternalArgsScreen(
    navArgs: InternalArgs,
) {
    Text("Internal args screen $navArgs")
}