package com.ramcosta.composedestinations.navargs

/**
 * Interface with behaviour for classes that can prepare a nonnull [T] type
 * into a string usable in the navigation route and can parse that same string
 * representation back into the [T] type.
 *
 * You can define an implementation for a specific type [T] and annotate it with
 * `@NavTypeSerializer` to make the generated code use it.
 *
 * If [T] is [android.os.Parcelable] then it will be saved in the Android [android.os.Bundle]
 * as a Parcelable. [java.io.Serializable] types are saved as Serializable.
 * Compose Destinations uses a default [DestinationsNavTypeSerializer] for both [android.os.Parcelable]
 * and [java.io.Serializable], so you don't need to create an explicit serializer, unless
 * you want to be able to deep link into screens that receive those types of arguments.
 *
 * For all other types that you turn as navigation argument types with this, they will be saved in the
 * [android.os.Bundle] as Strings.
 */
interface DestinationsNavTypeSerializer<T: Any> {
    fun toRouteString(value: T): String

    fun fromRouteString(routeStr: String): T
}
