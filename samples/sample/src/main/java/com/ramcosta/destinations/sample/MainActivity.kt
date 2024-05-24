package com.ramcosta.destinations.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.ramcosta.destinations.sample.core.di.DependencyContainer
import com.ramcosta.destinations.sample.ui.theme.DestinationsTodoSampleTheme

val LocalDependencyContainer = staticCompositionLocalOf<DependencyContainer> {
    error("No dependency container provided!")
}

class MainActivity : ComponentActivity() {

    private val dependencyContainer by lazy { DependencyContainer(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DestinationsTodoSampleTheme {
                // A surface container using the 'background' color from the theme
                CompositionLocalProvider(LocalDependencyContainer provides dependencyContainer) {
                    SampleApp()
                }
            }
        }
    }
}
