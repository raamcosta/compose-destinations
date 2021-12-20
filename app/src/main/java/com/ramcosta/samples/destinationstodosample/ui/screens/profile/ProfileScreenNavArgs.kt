package com.ramcosta.samples.destinationstodosample.ui.screens.profile

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

val DEFAULT_GROUP : String? = null

data class ProfileScreenNavArgs(
    val id: Long,
    val groupName: String? = DEFAULT_GROUP,
    val stuff: Stuff = Stuff.STUFF1,
    val things: Things = Things(),
)

enum class Stuff {
    STUFF1,
    STUFF2
}

@Parcelize
data class Things(
    val thing1: String = "QWEQWEQWE",
    val thing2: String = "ASDASDASD"
) : Parcelable
