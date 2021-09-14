package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME

//region anchors
internal const val SYMBOL_QUALIFIED_NAME = "[SYMBOL_QUALIFIED_NAME]"
internal const val DESTINATION_NAME = "[DESTINATION_NAME]"
internal const val COMPOSED_ROUTE = "[COMPOSED_ROUTE]"
internal const val NAV_ARGUMENTS = "[NAV_ARGUMENTS]"
internal const val WITH_ARGS_METHOD = "[WITH_ARGS_METHOD]"
internal const val CONTENT_FUNCION_CODE = "[CONTENT_FUNCION_CODE]"
//endregion

internal val destinationTemplate="""
package $PACKAGE_NAME

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.compose.material.ScaffoldState
import androidx.navigation.compose.navArgument
import $SYMBOL_QUALIFIED_NAME

object $DESTINATION_NAME: $GENERATED_DESTINATION {

    override val route get() = "$COMPOSED_ROUTE"
    $NAV_ARGUMENTS
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState?
    ) {
        $CONTENT_FUNCION_CODE
    }
    $WITH_ARGS_METHOD
}
""".trimIndent()