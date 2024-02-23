package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import kotlin.reflect.KClass

@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class ExternalDestination<T: DestinationSpec>(
    val deepLinks: Array<DeepLink> = [],
    val style: KClass<out DestinationStyle> = Nothing::class,
    val wrappers: Array<KClass<out DestinationWrapper>> = [],
    val start: Boolean = false,
)