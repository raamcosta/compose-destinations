package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route
import kotlin.reflect.KClass

/**
 * TODO RACOSTA
 */
annotation class ExternalRoutes(
    val destinations: Array<KClass<out DestinationSpec>> = [],
    val nestedNavGraphs: Array<KClass<out NavGraphSpec>> = [],
    val startRoute: KClass<out Route> = Nothing::class
)