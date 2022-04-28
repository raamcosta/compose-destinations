package com.ramcosta.samples.playground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.ramcosta.samples.playground.di.DependencyContainer

val LocalDependencyContainer = staticCompositionLocalOf<DependencyContainer> {
    error("No dependency container provided!")
}

class MainActivity : ComponentActivity() {

    private val dependencyContainer = DependencyContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(LocalDependencyContainer provides dependencyContainer) {
                DestinationsSampleApp()
            }
        }
    }
}