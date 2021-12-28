package com.ramcosta.composedestinations.navargs.serializable

import java.io.Serializable

/**
 * Interface with behaviour for classes that can prepare a [Serializable] type
 * into a string usable in the navigation route and can parse that same string
 * representation back into the [Serializable] type.
 *
 * You can define an implementation for a specific type [T] and annotate it with
 * `@NavTypeSerializer` to make the generated code use it.
 */
interface SerializableNavTypeSerializer<T : Serializable> {

    fun toRouteString(value: T): String

    fun fromRouteString(routeStr: String): T
}