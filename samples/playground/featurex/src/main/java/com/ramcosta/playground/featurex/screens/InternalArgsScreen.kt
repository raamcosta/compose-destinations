package com.ramcosta.playground.featurex.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility
import com.ramcosta.playground.featurex.FeatureXGraph
import kotlinx.serialization.Serializable


internal data class InternalArgs(
//    val internalClass: InternalSerializableArg,
    val arrayOfInternals: ArrayList<InternalSerializableArg>
)

@Serializable
internal data class InternalSerializableArg(
    val someValue: String
)

@Destination<FeatureXGraph>(
    navArgs = InternalArgs::class,
    visibility = CodeGenVisibility.INTERNAL
)
@Composable
internal fun InternalArgsScreen(
    navArgs: InternalArgs,
) {
    Text("Internal args screen $navArgs")
}