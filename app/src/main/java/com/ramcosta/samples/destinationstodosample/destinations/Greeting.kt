package com.ramcosta.samples.destinationstodosample.destinations

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
import androidx.navigation.NavController
import com.ramcosta.composedestinations.GreetingDestination
import com.ramcosta.composedestinations.ProfileDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.samples.destinationstodosample.title
import kotlinx.coroutines.launch

@Destination(route = "greeting", start = true)
@Composable
fun Greeting(
    navController: NavController,
    scaffoldState: ScaffoldState
) {
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = stringResource(id = GreetingDestination.title()),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navController.navigate(
                        //"settings/{arg0}/{arg5}?arg1={arg1}?arg2={arg2}?arg3={arg3}?arg4={arg4}"
                        ProfileDestination.withArgs(
                            arg1 = "stuff",
                            arg0 = 7L,
                            arg4 = "ARG4",
                            arg5 = true,
                            arg3 = "arg3333"
                        )
                    )
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