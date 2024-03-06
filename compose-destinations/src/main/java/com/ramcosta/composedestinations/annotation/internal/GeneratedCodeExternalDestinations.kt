package com.ramcosta.composedestinations.annotation.internal

import com.ramcosta.composedestinations.spec.DestinationSpec
import kotlin.reflect.KClass

/**
 * To be used by generated code.
 */
@Retention(AnnotationRetention.BINARY)
annotation class GeneratedCodeExternalDestinations(
    val destinations: Array<KClass<out DestinationSpec>>
)
