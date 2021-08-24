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
import com.ramcosta.samples.destinationstodosample.ui.theme.DestinationsTodoSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DestinationsTodoSampleTheme {
                Screens.Scaffold(
                    startDestination = GreetingDestination
                )
            }
        }
    }
}

@Screen(route = "greeting")
@Composable
fun Greeting(navController: NavController) {
    Column {
        Text(text = "Hello ${GreetingDestination.route}!")
        Button(onClick = { navController.navigateTo(SettingsDestination, "arg1" to "cena") {} }) {
            Text(text = "GO TO SETTINGS")
        }
    }
}

@Screen(route = "settings")
@Composable
fun Settings(navController: NavController, arg1: String? = "defaultArg") {
    Text(text = "Settings ${SettingsDestination.route} + arg1= $arg1!")
}