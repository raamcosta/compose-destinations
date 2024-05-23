package com.ramcosta.samples.playground.ui.screens

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.annotation.parameters.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.NavTypeSerializer
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import com.ramcosta.samples.playground.commons.SettingsGraph
import com.ramcosta.samples.playground.ui.screens.profile.ProfileScreenNavArgs
import com.ramcosta.samples.playground.ui.screens.profile.SerializableExampleWithNavTypeSerializer
import com.ramcosta.samples.playground.ui.screens.profile.Stuff
import com.ramcosta.samples.playground.ui.screens.wrappers.DrawerOpeningWrapper
import com.ramcosta.samples.playground.ui.screens.wrappers.HidingScreenWrapper
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Destination<SettingsGraph>
annotation class HidingScreenDestination(
    val wrappers: Array<KClass<out DestinationWrapper>> = [DrawerOpeningWrapper::class],
    val navArgs: KClass<*> = Nothing::class,
)

@HidingScreenDestination
annotation class HidingScreenDestination2(
    val wrappers: Array<KClass<out DestinationWrapper>> = [HidingScreenWrapper::class],
    val navArgs: KClass<*> = ProfileScreenNavArgs::class,
)

data class AnotherTestNavArgs(
    val asd: String
)

@HidingScreenDestination2(
    navArgs = AnotherTestNavArgs::class
)
@Composable
fun AnotherTestScreen() {
    Text("ANOTHER TEST")
}

@Destination<RootGraph>
@Composable
fun TestScreen(
    id: Long = 0,
    asd: String,
    stuff1: ArrayList<String> = arrayListOf(),
    stuff2: Array<Stuff>?,
    @Suppress("UNUSED_PARAMETER") stuffn: ArrayList<Stuff>?, //TODO RACOSTA
    stuff3: ArrayList<Color>? = arrayListOf(),
    stuff4: SerializableExampleWithNavTypeSerializer? = SerializableExampleWithNavTypeSerializer(),
    stuff5: Color,
    stuff6: OtherThings,
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(stuff5),
            textAlign = TextAlign.Center,
            text = """
            id = $id
            asd = $asd
            stuff1 = $stuff1
            stuff2 = ${stuff2.contentToString()}
            stuff3 = $stuff3
            stuff4 = $stuff4
            stuff5 = $stuff5
            stuff6 = $stuff6
        """.trimIndent()
        )

        Row(Modifier.fillMaxWidth()) {

            stuff3?.forEach {
                Text(
                    modifier = Modifier.weight(1f).background(it),
                    text = "$it"
                )
            }
        }
    }
}

@Parcelize
data class Things(
    val thingyOne: String = "thingy1",
    val thingyTwo: String = "thingy2",
) : Parcelable

@Serializable
data class OtherThings(
    val thatIsAThing: String,
    val thatIsAValueClass: ValueClass,
)

@JvmInline
@Serializable
value class ValueClass(val value: String)

@NavTypeSerializer
class ColorTypeSerializer : DestinationsNavTypeSerializer<Color> {
    override fun toRouteString(value: Color): String =
        value.toArgb().toString()

    override fun fromRouteString(routeStr: String): Color =
        Color(routeStr.toInt())
}

// Just to test generation of multiple similar destinations
@Suppress("UNUSED_PARAMETER")
@Destination<RootGraph>
@Composable
fun TestScreen2(
    id: Long = 0,
    stuff1: ArrayList<String> = arrayListOf(),
    stuff2: Array<Stuff>?,
    stuff3: ArrayList<Color>? = arrayListOf(),
    stuff4: SerializableExampleWithNavTypeSerializer? = SerializableExampleWithNavTypeSerializer(),
    stuff5: Color,
    stuff6: OtherThings,
) {
}

@Destination<RootGraph>(
    deepLinks = [DeepLink(uriPattern = "something://$FULL_ROUTE_PLACEHOLDER")]
)
@Composable
fun TestScreen3(

) {
}
