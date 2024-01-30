package com.ramcosta.playground.featurey.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NoParent

data class SomeArgsInHere(
    val asd: String,
    val list: ArrayList<String>
)

@Destination<NoParent>(
    navArgs = SomeArgsInHere::class
)
@Composable
fun PublicFeatureYSideScreen() {
    Text("PublicFeatureYSideScreen")
}