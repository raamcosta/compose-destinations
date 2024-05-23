package com.ramcosta.composedestinations.navargs

/**
 * You can annotate a [com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer]
 * to signal the code generating task to consider the annotated class as a parser/serializer for a
 * given type that will be used as navigation argument.
 *
 * It allows you to make any type be considered a navigation argument type.
 *
 * Also, it controls how the complex type will be represented in the route which can be good if you
 * intend to navigate to the screen using the argument type through a deep link.
 *
 * @see DestinationsNavTypeSerializer
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class NavTypeSerializer