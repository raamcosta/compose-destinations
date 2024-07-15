package com.ramcosta.samples.playground.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.featurey.destinations.FeatureYHomeDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.onResult
import com.ramcosta.playground.featurey.screens.InternalBackResult
import com.ramcosta.samples.playground.LocalToaster
import com.ramcosta.samples.playground.commons.requireTitle
import com.ramcosta.samples.playground.ui.screens.destinations.FeedDestination

@OptIn(ExperimentalAnimationApi::class)
@Destination<RootGraph>
@Composable
fun AnimatedVisibilityScope.Feed(
    navigator: DestinationsNavigator,
    featYResult: ResultRecipient<FeatureYHomeDestination, InternalBackResult>,
) {
    println("Feed | running? " + transition.isRunning)

    val toaster = LocalToaster.current
    featYResult.onResult {
        toaster.show("featY result = $it")
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(
                text = FeedDestination.requireTitle
            )

            Button(
                onClick = {
                    TODO("not on kmp!")
//                    navigator.navigate(
//                        OtherActivityDestination(
//                            otherThing = "testing",
//                            color = Color.Magenta
//                        )
//                    )
                }
            ) {
                Text(
                    text = "Go to Other activity!"
                )
            }

            Button(
                onClick = {
                    navigator.navigate(FeatureYHomeDestination)
                }
            ) {
                Text(
                    text = "Go to FeatureY!"
                )
            }
        }
    }
}