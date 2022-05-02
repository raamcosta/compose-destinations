package com.ramcosta.destinations.sample.ui.composables

import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import com.ramcosta.destinations.sample.destinations.Destination

@Composable
fun TopBar(destination: Destination) {
    TopAppBar { Text(text = destination.javaClass.simpleName.removeSuffix("Destination")) }
}