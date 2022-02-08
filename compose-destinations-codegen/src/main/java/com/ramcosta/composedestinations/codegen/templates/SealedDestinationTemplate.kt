package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.codeGenDestination
import com.ramcosta.composedestinations.codegen.codeGenNoArgsDestination
import com.ramcosta.composedestinations.codegen.moduleName

val typeAliasDestination = "${moduleName}Destination"

val sealedDestinationTemplate = """
package $codeGenBasePackageName.destinations

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $CORE_PACKAGE_NAME.spec.*

/**
 * Handy typealias of [$codeGenDestination] when you don't
 * care about the generic type (probably most cases for app's use)
 */
typealias $typeAliasDestination = $codeGenDestination<*>

/**
 * $codeGenDestination is a sealed version of [$CORE_DESTINATION_SPEC]
 */
sealed interface $codeGenDestination<T>: $CORE_DESTINATION_SPEC<T>

/**
 * Interface for all $codeGenDestination with no navigation arguments
 */
sealed interface $codeGenNoArgsDestination: $codeGenDestination<Unit>, $CORE_DIRECTION_DESTINATION_SPEC {
    
    override fun argsFrom(navBackStackEntry: NavBackStackEntry) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}

""".trimIndent()