package com.ramcosta.destinations.sample

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.destinations.sample.ui.composables.SampleScaffold

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@Composable
fun SampleApp() {
    val engine = rememberAnimatedNavHostEngine()
    val navController = engine.rememberNavController()

    SampleScaffold(
        navController = navController,
        topBar = {

        },
        bottomBar = {

        }
    ) {
        DestinationsNavHost(
            engine = engine,
            navController = navController,
            navGraph = NavGraphs.root,
            modifier = Modifier.padding(it),
        )
    }
}