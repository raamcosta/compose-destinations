package com.ramcosta.samples.playground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.ramcosta.composedestinations.annotation.ActivityDestination
import com.ramcosta.samples.playground.commons.SettingsGraph

data class OtherActivityNavArgs(
    val otherThing: String,
    val color: Color
)

@ActivityDestination<SettingsGraph>(
    navArgs = OtherActivityNavArgs::class,
)
annotation class OtherActivityDestinantion

@OtherActivityDestinantion
class OtherActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = OtherActivityDestination.argsFrom(intent)
        println("OtherActivity args = $args")

        setContentView(ComposeView(this).apply {
            setContent {
                Column(
                    modifier = Modifier.background(args.color)
                ) {
                    Text("OTHER ACTIVITY")
                    Text("ARGS: \n$args")
                }
            }
        })
    }
}