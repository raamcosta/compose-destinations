package com.ramcosta.destinations.sample.login

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyle

@Destination(
    style = DestinationStyle.BottomSheet::class
)
@Composable
fun LoginScreen() {
    Text("LOGIN")
}