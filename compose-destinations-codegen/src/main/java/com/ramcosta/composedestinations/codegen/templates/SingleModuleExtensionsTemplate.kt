package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.writers.typeAliasNavGraph

val singleModuleExtensionsTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName",
    imports = setOfImportable(
        "androidx.compose.runtime.Composable",
        "androidx.compose.runtime.State",
        "androidx.compose.runtime.collectAsState",
        "androidx.navigation.NavBackStackEntry",
        "androidx.navigation.NavController",
        "$codeGenBasePackageName.destinations.*",
        "$CORE_PACKAGE_NAME.spec.*",
        "$CORE_PACKAGE_NAME.utils.startDestination",
        "$CORE_PACKAGE_NAME.utils.destination",
        "$CORE_PACKAGE_NAME.utils.navGraph",
        "$CORE_PACKAGE_NAME.utils.currentDestinationFlow",
        "kotlinx.coroutines.flow.Flow",
        "kotlinx.coroutines.flow.map",
    ),
    sourceCode = """

/**
 * If this [Route] is a [$typeAliasDestination], returns it
 *
 * If this [Route] is a [$typeAliasNavGraph], returns its
 * start [$typeAliasDestination].
 */
public val Route.startAppDestination: $typeAliasDestination
    get() = startDestination as $typeAliasDestination

/**
 * Finds the [$typeAliasDestination] correspondent to this [NavBackStackEntry].
 * Some [NavBackStackEntry] are not [$typeAliasDestination], but are [$typeAliasNavGraph] instead.
 * If you want a method that works for both, use [route] extension function instead.
 *
 * Use this ONLY if you're sure your [NavBackStackEntry] corresponds to a [$typeAliasDestination],
 * for example when converting from "current NavBackStackEntry", since a [$typeAliasNavGraph] is never
 * the "current destination" shown on screen.
 */
public fun NavBackStackEntry.appDestination(): $typeAliasDestination {
    return destination() as $typeAliasDestination
}

/**
 * Emits the currently active [$typeAliasDestination] whenever it changes. If
 * there is no active [$typeAliasDestination], no item will be emitted.
 */
public val NavController.appCurrentDestinationFlow: Flow<$typeAliasDestination>
    get() = currentDestinationFlow.map { it as $typeAliasDestination }

/**
 * Gets the current [$typeAliasDestination] as a [State].
 */
@Composable
public fun NavController.appCurrentDestinationAsState(): State<$typeAliasDestination?> {
    return appCurrentDestinationFlow.collectAsState(initial = null)
}

""".trimIndent()
)
