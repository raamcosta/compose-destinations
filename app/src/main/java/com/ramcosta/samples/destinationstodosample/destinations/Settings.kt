package com.ramcosta.samples.destinationstodosample.destinations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.ProfileDestination
import com.ramcosta.composedestinations.annotation.Destination

@Destination(route = "profile")
@Composable
fun Profile(
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
            text = "Profile ${ProfileDestination.route} " +
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