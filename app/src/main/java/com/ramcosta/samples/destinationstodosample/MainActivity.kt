package com.ramcosta.samples.destinationstodosample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.samples.destinationstodosample.ui.theme.DestinationsTodoSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DestinationsTodoSampleTheme {
                Destinations.Scaffold(
                    startDestination = GreetingDestination
                )
            }
        }
    }
}

@Destination(route = "greeting")
@Composable
fun Greeting(navController: NavController, scaffoldState: ScaffoldState) {
    Column {
        Text(text = "Hello ${GreetingDestination.route}!")
        Button(
            onClick = {
                navController.navigate(
                    //"settings/{arg0}/{arg5}?arg1={arg1}?arg2={arg2}?arg3={arg3}?arg4={arg4}"
                    SettingsDestination.withArgs(
                        arg1 = "cena",
                        arg0 = 7L,
                        arg4 = "ARG4",
                        arg5 = true,
                        arg3 = "arg3333"
                    )
                )
            }
        ) {
            Text(text = "GO TO SETTINGS")
        }
    }
}

@Destination(route = "settings")
@Composable
fun Settings(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry,
    arg0: Long,
    arg1: String? = "defaultArg",
    arg2: String = "lol",
    arg3: String?,
    arg4: String? = null,
    arg5: Boolean,
    arg6: Float = 77.0f,
) {
    Text(
        text = "Settings ${SettingsDestination.route} " +
                "\n\nARGS =" +
                "\n " +
                "\n arg0= $arg0" +
                "\n arg1= $arg1" +
                "\n arg2= $arg2" +
                "\n arg3= $arg3" +
                "\n arg4= $arg4" +
                "\n arg5= $arg5" +
                "\n arg6= $arg6"
    )
}

@Destination("profile")
@Composable
fun Profile() {
    Box(Modifier.fillMaxSize()) {
        Text("PROFILE SCREEN", modifier = Modifier.align(Alignment.Center))
    }
}