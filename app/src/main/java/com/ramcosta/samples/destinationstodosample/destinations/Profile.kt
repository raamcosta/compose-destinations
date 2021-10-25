package com.ramcosta.samples.destinationstodosample.destinations

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.ProfileDestination
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.samples.destinationstodosample.destinations.styles.ProfileTransitions

const val DEFAULT_ID = "DEFAULT_ID"
const val DEFAULT_ID2 = 2L

//@OptIn(ExperimentalAnimationApi::class)
@Destination(
    deepLinks = [
        DeepLink(uriPattern = "https://destinationssample.com/$FULL_ROUTE_PLACEHOLDER")
    ],
//    style = ProfileTransitions::class
)
@Composable
fun Profile(
//fun AnimatedVisibilityScope.Profile(
    id: String? = DEFAULT_ID,
    id2: Long = DEFAULT_ID2
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Green)
    ) {
        Text(
            text = "Profile route: ${ProfileDestination.route} " +
                    "\n\nARGS =" +
                    "\n " +
                    "\n profile id= $id" +
                    "\n profile id2= $id2"
        )
    }
}