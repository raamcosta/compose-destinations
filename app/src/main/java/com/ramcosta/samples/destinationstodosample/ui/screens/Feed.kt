package com.ramcosta.samples.destinationstodosample.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.samples.destinationstodosample.commons.requireTitle
import com.ramcosta.samples.destinationstodosample.ui.screens.destinations.FeedDestination

@OptIn(ExperimentalAnimationApi::class)
@Destination
@Composable
fun Feed() {

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        Text(
            text = stringResource(id = FeedDestination.requireTitle),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}