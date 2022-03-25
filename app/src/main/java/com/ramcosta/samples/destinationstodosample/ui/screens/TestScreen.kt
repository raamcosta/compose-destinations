package com.ramcosta.samples.destinationstodosample.ui.screens

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navargs.NavTypeSerializer
import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.samples.destinationstodosample.ui.screens.profile.SerializableExample
import com.ramcosta.samples.destinationstodosample.ui.screens.profile.Stuff
import kotlinx.parcelize.Parcelize

@Destination
@Composable
fun TestScreen(
    id: String,
    stuff1: Long = 1L,
    stuff2: Stuff?,
    stuff3: Things? = Things(),
    stuff4: SerializableExample? = SerializableExample(),
    stuff5: Color,
) {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .background(stuff5),
        textAlign = TextAlign.Center,
        text = """
            id = $id 
            stuff1 = $stuff1
            stuff2 = $stuff2
            stuff3 = $stuff3
            stuff4 = $stuff4
            stuff5 = $stuff5
        """.trimIndent()
    )
}

@Parcelize
data class Things(
    val thingyOne: String = "thingy1",
    val thingyTwo: String = "thingy2",
) : Parcelable

@NavTypeSerializer
class ColorTypeSerializer : DestinationsNavTypeSerializer<Color> {
    override fun toRouteString(value: Color): String =
        value.toArgb().toString()

    override fun fromRouteString(routeStr: String): Color =
        Color(routeStr.toInt())
}
