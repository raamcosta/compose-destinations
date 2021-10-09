package com.ramcosta.samples.destinationstodosample.destinations

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.DestinationStyle
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.GreetingDestination
import com.ramcosta.composedestinations.ProfileScreenDestination
import com.ramcosta.composedestinations.GoToProfileConfirmationDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.samples.destinationstodosample.destinations.transitions.GreetingTransitions
import com.ramcosta.samples.destinationstodosample.requireTitle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Destination(
    start = true,
    style = GreetingTransitions::class
)
@Composable
fun Greeting(
    navigator: DestinationsNavigator,
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = stringResource(id = GreetingDestination.requireTitle),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navigator.navigate(GoToProfileConfirmationDestination)
                }
            ) {
                Text(text = "GO TO PROFILE")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    coroutineScope.launch { scaffoldState.drawerState.open() }
                }
            ) {
                Text(text = "OPEN DRAWER")
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Destination(style = DestinationStyle.Dialog::class)
@Composable
fun GoToProfileConfirmation(
    navigator: DestinationsNavigator
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Are you sure you want to go to Profile Screen?")
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                navigator.navigate(ProfileScreenDestination.withArgs(id = 1L)) {
                    popUpTo(GoToProfileConfirmationDestination.route) {
                        inclusive = true
                    }
                }
            }
        ) {
            Text(text = "Yes!")
        }
    }
}