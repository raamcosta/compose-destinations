package com.ramcosta.samples.destinationstodosample.ui.screens.profile

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.NavTypeSerializer
import kotlinx.parcelize.Parcelize
import java.io.Serializable

const val DEFAULT_GROUP: String = ""

data class ProfileScreenNavArgs(
    val id: Long,
    val stuff: Stuff = Stuff.STUFF1,
    val groupName: String = DEFAULT_GROUP,
    val things: ArgumentThings?,
    val serializableExample: SerializableExample? = null,
    val color: Color
)

enum class Stuff {
    STUFF1,
    STUFF2
}

interface ArgumentThings: Parcelable

@Parcelize
data class Things(
    val thing1: String = "QWEQWEQWE",
    val thing2: String = "ASDASDASD"
) : ArgumentThings

data class SerializableExample(
    val thing1: String = "SERIALIZABLE/11/1",
    val thing2: String = "qweqew?asd=SERIALIZABLE222"
) : Serializable


@NavTypeSerializer
object ThingsNavTypeSerializer : DestinationsNavTypeSerializer<Things> {

    override fun toRouteString(value: Things): String {
        return "${value.thing1};${value.thing2}"
    }

    override fun fromRouteString(routeStr: String): Things {
        return routeStr.split(";").run {
            Things(get(0), get(1))
        }
    }
}

@NavTypeSerializer
class SerializableExampleSerializer : DestinationsNavTypeSerializer<SerializableExample> {

    override fun toRouteString(value: SerializableExample): String {
        return "${value.thing1};${value.thing2}"
    }

    override fun fromRouteString(routeStr: String): SerializableExample {
        return routeStr.split(";").run {
            SerializableExample(get(0), get(1))
        }
    }

}