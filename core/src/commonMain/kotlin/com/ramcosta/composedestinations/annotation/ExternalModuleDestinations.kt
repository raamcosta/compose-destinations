package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.ModuleDestinationsContainer
import kotlin.reflect.KClass

/**
 * Annotation used to import all destinations from [T] [ModuleDestinationsContainer].
 * You can then override certain things about individual destinations, using
 * the [overriding] parameter.
 *
 * @param overriding can be used to pass individual [ExternalDestination]
 * to control parameters available with that annotation for that specific [DestinationSpec]
 */
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class ExternalModuleDestinations<T: ModuleDestinationsContainer>(
    val overriding: Array<OverrideDestination<out DestinationSpec>> = []
)

/**
 * Use [with] parameters to override them for [destination] specifically.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class OverrideDestination<T: DestinationSpec>(
    val destination: KClass<T>,
    val with: ExternalDestination<T>,
)
