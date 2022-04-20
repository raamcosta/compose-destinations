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
    val groupName: String? = DEFAULT_GROUP,
    val whatever: Int? = 12333,
    val things: ArgumentThings? = null,
    val thingsWithNavTypeSerializer: Things? = null,
    val serializableExample: SerializableExample? = SerializableExample(),
    val serializableExampleWithNavTypeSerializer: SerializableExampleWithNavTypeSerializer? = null,
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

data class SerializableExampleWithNavTypeSerializer(
    val thing1: String = "SERIALIZABLE/11/1",
    val thing2: String = "qweqew?asd=SERIALIZABLE222"
) : Serializable

data class SerializableExample(
    val thing1: String = "SerializableExampleWithNoNavTypeSerializer/11/1",
    val thing2: String = "qweqew?asd=SerializableExampleWithNoNavTypeSerializer"
) : Serializable


@NavTypeSerializer
object ThingsNavTypeSerializer : DestinationsNavTypeSerializer<Things> {

    override fun toRouteString(value: Things): String {
        return "${value.thing1};${value.thing2}"
    }

    override fun fromRouteString(routeStr: String): Things {
        val things = routeStr.split(";")
        return Things(things[0], things[1])
    }
}

@NavTypeSerializer
class SerializableExampleSerializer : DestinationsNavTypeSerializer<SerializableExampleWithNavTypeSerializer> {

    override fun toRouteString(value: SerializableExampleWithNavTypeSerializer): String {
        return "${value.thing1};${value.thing2}"
    }

    override fun fromRouteString(routeStr: String): SerializableExampleWithNavTypeSerializer {
        return routeStr.split(";").run {
            SerializableExampleWithNavTypeSerializer(get(0), get(1))
        }
    }

}