package com.ramcosta.samples.playground.ui.screens

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.generated.featurey.destinations.FeatureYHomeDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.onResult
import com.ramcosta.samples.playground.commons.requireTitle
import com.ramcosta.samples.playground.ui.screens.destinations.FeedDestination
import com.ramcosta.samples.playground.ui.screens.destinations.OtherActivityDestination

@OptIn(ExperimentalAnimationApi::class)
@Destination<RootNavGraph>
@Composable
fun AnimatedVisibilityScope.Feed(
    navigator: DestinationsNavigator,
    featYResult: ResultRecipient<FeatureYHomeDestination, Boolean>,
) {
    Log.d("Feed", "running? " + transition.isRunning)

    val context = LocalContext.current
    featYResult.onResult {
        Toast.makeText(context, "featY result = $it", Toast.LENGTH_SHORT).show()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(
                text = stringResource(id = FeedDestination.requireTitle)
            )

            Button(
                onClick = {
                    navigator.navigate(
                        OtherActivityDestination(
                            otherThing = "testing",
                            color = Color.Magenta
                        )
                    )
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