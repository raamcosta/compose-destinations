package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.CORE_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NO_ARGS_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME

val sealedDestinationTemplate = """
package $PACKAGE_NAME.destinations

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $PACKAGE_NAME.spec.DestinationSpec
import $PACKAGE_NAME.spec.Routed

/**
 * Handy typealias of [$GENERATED_DESTINATION] when you don't
 * care about the generic type (probably most cases for app's use)
 */
typealias Destination = $GENERATED_DESTINATION<*>

/**
 * $GENERATED_DESTINATION is a sealed version of [$CORE_DESTINATION_SPEC]
 */
sealed interface $GENERATED_DESTINATION<T>: $CORE_DESTINATION_SPEC<T>

/**
 * Interface for all $GENERATED_DESTINATION with no navigation arguments
 */
sealed interface $GENERATED_NO_ARGS_DESTINATION: $GENERATED_DESTINATION<Unit>, Routed {
    
    override fun argsFrom(navBackStackEntry: NavBackStackEntry) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}

""".trimIndent()