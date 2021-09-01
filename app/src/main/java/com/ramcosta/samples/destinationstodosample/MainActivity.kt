package com.ramcosta.samples.destinationstodosample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
                    topBar = {
                        MyTopBar(destination = it)
                    },
                    bottomBar = {
                        MyBottomBar(destination = it)
                    },
                    modifierForDestination = { destination, padding ->
                        destination.destinationPadding(parentPadding = padding)
                    }
                )
            }
        }
    }
}

fun DestinationSpec.destinationPadding(parentPadding: PaddingValues): Modifier {
    return when (this) {
        GreetingDestination -> Modifier.padding(parentPadding)
        ProfileDestination -> Modifier.padding(parentPadding)
        SettingsDestination -> Modifier.padding(
            start = 0.dp,
            end = 0.dp,
            top = parentPadding.calculateTopPadding() + 10.dp,
            bottom = parentPadding.calculateBottomPadding() + 10.dp
        )
        FeedDestination -> Modifier.padding(parentPadding)
    }
}

@StringRes
fun DestinationSpec.title(): Int {
    return when (this) {
        GreetingDestination -> R.string.greeting_screen
        ProfileDestination -> R.string.profile_screen
        SettingsDestination -> R.string.settings_screen
        FeedDestination -> R.string.feed_screen
    }
}

@Composable
fun MyTopBar(
    destination: DestinationSpec
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .background(MaterialTheme.colors.primary)
    ) {
        Text(
            text = stringResource(destination.title()),
            modifier = Modifier.align(Alignment.Center),
            color = Color.White
        )
    }
}

@Composable
fun MyBottomBar(
    destination: DestinationSpec
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .background(MaterialTheme.colors.primary)
    ) {
        Text(
            text = stringResource(destination.title()),
            modifier = Modifier.align(Alignment.Center),
            color = Color.White
        )
    }
}

@Destination(route = "greeting", start = true)
@Composable
fun Greeting(
    navController: NavController,
    scaffoldState: ScaffoldState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        Text(text = "Hello ${GreetingDestination.route}!")
        Button(
            onClick = {
                navController.navigate(
                    //"settings/{arg0}/{arg5}?arg1={arg1}?arg2={arg2}?arg3={arg3}?arg4={arg4}"
                    SettingsDestination.withArgs(
                        arg1 = "stuff",
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Green)
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
}

@Destination("profile")
@Composable
fun Profile() {
    Box(Modifier.fillMaxSize()) {
        Text("PROFILE SCREEN", modifier = Modifier.align(Alignment.Center))
    }
}

@Destination("feed")
@Composable
fun Feed() {
    Box(Modifier.fillMaxSize()) {
        Text("FEED SCREEN", modifier = Modifier.align(Alignment.Center))
    }
}