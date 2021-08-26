package com.ramcosta.samples.destinationstodosample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
fun Greeting(navController: NavController) {
    Column {
        Text(text = "Hello ${GreetingDestination.route}!")
        Button(
            onClick = {
                navController.navigateTo(
                    SettingsDestination,
                    "arg1" to "cena",
                    "arg0" to 7,
                    "arg4" to "ARG4",
                    "arg5" to "ARG5"
                ) {

                }
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
    arg1: String? = "defaultArg",
    arg2: String = "lol",
    arg0: Int,
    arg5: String,
    arg3: String?,
    arg4: String? = null
) {
    Text(
        text = "Settings ${SettingsDestination.route} " +
                "\n\nARGS =" +
                "\n arg1= $arg1!" +
                " + arg2= $arg2!" +
                " + arg0= $arg0!" +
                " + arg3= $arg3!" +
                " + arg4= $arg4!" +
                " + arg5= $arg5!"
    )
}