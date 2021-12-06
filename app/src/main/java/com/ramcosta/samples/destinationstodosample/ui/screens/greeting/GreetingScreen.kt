package com.ramcosta.samples.destinationstodosample.ui.screens.greeting

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.GoToProfileConfirmationDestination
import com.ramcosta.composedestinations.TestScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.samples.destinationstodosample.R
import com.ramcosta.samples.destinationstodosample.commons.DrawerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Destination(
    start = true,
    style = GreetingTransitions::class
)
@Composable
fun GreetingScreen(
    navigator: DestinationsNavigator,
    drawerController: DrawerController,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    uiEvents: GreetingUiEvents,
    uiState: GreetingUiState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.greeting + " Screen!",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    uiEvents.onNewGreetingClicked()
                }
            ) {
                Text(
                    text = stringResource(R.string.new_greeting)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navigator.navigate(GoToProfileConfirmationDestination)
                }
            ) {
                Text(text = stringResource(R.string.go_to_profile))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navigator.navigate(TestScreenDestination(id = "test-id"))
                }
            ) {
                Text(text = stringResource(R.string.go_to_test_screen))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    coroutineScope.launch { drawerController.open() }
                }
            ) {
                Text(text = stringResource(R.string.open_drawer))
            }
        }
    }
}