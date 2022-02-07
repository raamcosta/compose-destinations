package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.generatedDestination
import com.ramcosta.composedestinations.codegen.generatedNoArgsDestination
import com.ramcosta.composedestinations.codegen.moduleName

val typeAliasDestination = "${moduleName}Destination"

val sealedDestinationTemplate = """
package $codeGenBasePackageName.destinations

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $CORE_PACKAGE_NAME.spec.DestinationSpec
import $CORE_PACKAGE_NAME.spec.DirectionDestinationSpec

/**
 * Handy typealias of [$generatedDestination] when you don't
 * care about the generic type (probably most cases for app's use)
 */
typealias $typeAliasDestination = $generatedDestination<*>

/**
 * $generatedDestination is a sealed version of [$CORE_DESTINATION_SPEC]
 */
sealed interface $generatedDestination<T>: $CORE_DESTINATION_SPEC<T>

/**
 * Interface for all $generatedDestination with no navigation arguments
 */
sealed interface $generatedNoArgsDestination: $generatedDestination<Unit>, DirectionDestinationSpec {
    
    override fun argsFrom(navBackStackEntry: NavBackStackEntry) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}

""".trimIndent()