package com.ramcosta.samples.playground.ui.screens.profile

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.NavTypeSerializer
import kotlinx.parcelize.Parcelize
import java.io.Serializable

const val DEFAULT_GROUP: String = ""

data class ProfileScreenNavArgs(
    val id: Long,
    val source: SomeSource = aSource(),
    val sourceId: SomeSource.Id? = someSourceId(),
    val stuff: Stuff = Stuff.STUFF1,
    val stuff2: Array<Stuff> = stuffs2(),
    val stuff3: ArrayList<Stuff> = stuffs3(),
    val groupName: String? = DEFAULT_GROUP,
    val whatever: Int? = 12333,
    val things: ArgumentThings? = null,
    val valueClass: ValueClassArg,
    val thingsWithNavTypeSerializer: Things? = null,
    val serializableExample: SerializableExample? = SerializableExample(),
    val serializableExampleWithNavTypeSerializer: SerializableExampleWithNavTypeSerializer? = null,
    val color: Color
)

fun stuffs3() = arrayListOf(Stuff.STUFF1)

fun stuffs2() = arrayOf(Stuff.STUFF1)

fun aSource() = SomeSource.ASource(SomeSource.Id("source-id"))

fun someSourceId() = SomeSource.Id("source-id")

@kotlinx.serialization.Serializable
sealed interface SomeSource {

    @kotlinx.serialization.Serializable
    data class ASource(val id: Id): SomeSource

    @kotlinx.serialization.Serializable
    data class AnotherSource(val id: Id): SomeSource

    @JvmInline
    @kotlinx.serialization.Serializable
    value class Id(val value: String)
}

@JvmInline
value class ValueClassArg(val value: String)

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