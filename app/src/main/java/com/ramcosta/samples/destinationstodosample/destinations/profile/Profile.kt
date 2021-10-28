package com.ramcosta.samples.destinationstodosample.destinations.profile

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.samples.destinationstodosample.destinations.styles.ProfileTransitions

val DEFAULT_GROUP : String? = null
const val DEFAULT_ID = 2L

@OptIn(ExperimentalAnimationApi::class)
@Destination(
    deepLinks = [
        DeepLink(uriPattern = "https://destinationssample.com/$FULL_ROUTE_PLACEHOLDER")
    ],
    style = ProfileTransitions::class,
    navArgsDelegate = ProfileScreenNavArgs::class
)
@Composable
fun AnimatedVisibilityScope.ProfileScreen(
    uiState: ProfileUiState,
    uiEvents: ProfileUiEvents
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Green)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile id: ${uiState.id}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Belongs to: ${uiState.groupName}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${uiState.likeCount} likes"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    uiEvents.onLikeButtonClick()
                },
            ) {
                Text(text = "like")
            }
        }
    }
}