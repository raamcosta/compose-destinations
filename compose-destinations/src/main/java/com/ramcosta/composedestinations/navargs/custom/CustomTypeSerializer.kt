package com.ramcosta.composedestinations.navargs.custom

/**
 * Interface with behaviour for classes that can prepare an [Any] type
 * into a string usable in the navigation route and can parse that same string
 * representation back into the [Any] type.
 *
 * You can define an implementation for a specific type [T] and annotate it with
 * `@NavTypeSerializer` to make the generated code use it.
 */
interface CustomTypeSerializer<T: Any> {
    fun toRouteString(value: T): String

    fun fromRouteString(routeStr: String): T
}
