package com.ramcosta.samples.playground.ui.screens.greeting

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.samples.playground.R
import com.ramcosta.samples.playground.commons.DrawerController
import com.ramcosta.samples.playground.ui.screens.OtherThings
import com.ramcosta.samples.playground.ui.screens.ValueClass
import com.ramcosta.samples.playground.ui.screens.destinations.GoToProfileConfirmationDestination
import com.ramcosta.samples.playground.ui.screens.destinations.ProfileScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.TestScreenDestination
import com.ramcosta.samples.playground.ui.screens.profile.Stuff
import com.ramcosta.samples.playground.ui.screens.profile.Things
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@RootNavGraph(start = true)
@Destination(style = GreetingTransitions::class)
@Composable
fun GreetingScreen(
    navigator: DestinationsNavigator,
    drawerController: DrawerController,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    uiEvents: GreetingUiEvents,
    uiState: GreetingUiState,
    resultRecipient: ResultRecipient<GoToProfileConfirmationDestination, Boolean>
) {
    val context = LocalContext.current
    resultRecipient.onNavResult { result ->
        Toast.makeText(context, "result? = $result", Toast.LENGTH_LONG).show()
        println("go? $result")
        when (result) {
            is NavResult.Canceled -> println("canceled!!")
            is NavResult.Value -> if (result.value) {
                navigator.navigate(
                    ProfileScreenDestination(
                        id = 3,
                        whatever = null,
                        groupName = "{groupName}",
                        stuff = Stuff.STUFF2,
                        things = Things(),
                        color = Color.Black
                    )
                )
            }
        }
    }

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
                    navigator.navigate(
                        TestScreenDestination(
                            id = "test-id",
                            stuff1 = arrayListOf("%sqwe", "asd", "4", "zxc"),
                            stuff2 = arrayOf(Stuff.STUFF2, Stuff.STUFF2 ,Stuff.STUFF1),
                            stuff3 = arrayListOf(Color.Blue, Color.Red, Color.Green, Color.Cyan),
                            stuff5 = Color.DarkGray,
                            stuff6 = OtherThings(
                                thatIsAThing = "What a Thing!!",
                                thatIsAValueClass = ValueClass(
                                    value = "That is the value of the value class!",
                                )
                            )
                        )
                    )
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
