package com.ramcosta.samples.destinationstodosample.ui.screens.profile

import android.os.Parcel
import android.os.Parcelable

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

data class Things(
    val thing1: String = "QWEQWEQWE",
    val thing2: String = "ASDASDASD"
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(thing1)
        parcel.writeString(thing2)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Things> {

        override fun createFromParcel(parcel: Parcel): Things {
            return Things(parcel)
        }
        override fun newArray(size: Int): Array<Things?> {
            return arrayOfNulls(size)
        }
    }
}
