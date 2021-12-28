package com.ramcosta.composedestinations.navargs

/**
 * You can annotate either a [com.ramcosta.composedestinations.navargs.parcelable.ParcelableNavTypeSerializer]
 * or a [com.ramcosta.composedestinations.navargs.serializable.SerializableNavTypeSerializer] to
 * signal the code generating task to consider your class as a parser for a given type that will
 * be used as navigation argument.
 *
 * It controls how the complex type will be represented in the route which can be good if you intend
 * to navigate to the screen using the argument type through a deep link.
 * It can also provide better performance especially if the argument is [Serializable].
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NavTypeSerializer