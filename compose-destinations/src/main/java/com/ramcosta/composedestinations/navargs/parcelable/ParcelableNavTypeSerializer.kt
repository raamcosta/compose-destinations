package com.ramcosta.composedestinations.navargs.parcelable

import android.os.Parcelable

/**
 * Interface with behaviour for classes that can prepare a [Parcelable] type
 * into a string usable in the navigation route and can parse that same string
 * representation back into the [Parcelable] type.
 *
 * You can define an implementation for a specific type [T] and annotate it with
 * `@NavTypeSerializer` to make the generated code use it.
 */
interface ParcelableNavTypeSerializer<T : Parcelable> {

    fun toRouteString(value: T): String

    fun fromRouteString(routeStr: String, jClass: Class<out T>): T
}