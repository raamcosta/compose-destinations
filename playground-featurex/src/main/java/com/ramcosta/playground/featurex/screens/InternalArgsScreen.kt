package com.ramcosta.playground.featurex.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.paramtypes.CodeGenVisibility
import com.ramcosta.playground.featurex.FeatureXNavGraph
import kotlinx.serialization.Serializable


internal data class InternalArgs(
//    val internalClass: InternalSerializableArg,
    val arrayOfInternals: ArrayList<InternalSerializableArg>
)

@Serializable
internal data class InternalSerializableArg(
    val someValue: String
)

@FeatureXNavGraph
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