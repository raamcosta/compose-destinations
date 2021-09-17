package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME

//region anchors
internal const val ADDITIONAL_IMPORTS = "[ADDITIONAL_IMPORTS]"
internal const val DESTINATION_NAME = "[DESTINATION_NAME]"
internal const val COMPOSED_ROUTE = "[COMPOSED_ROUTE]"
internal const val NAV_ARGUMENTS = "[NAV_ARGUMENTS]"
internal const val DEEP_LINKS = "[DEEP_LINKS]"
internal const val WITH_ARGS_METHOD = "[WITH_ARGS_METHOD]"
internal const val CONTENT_FUNCTION_CODE = "[CONTENT_FUNCTION_CODE]"
//endregion

internal val destinationTemplate="""
package $PACKAGE_NAME

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.compose.material.ScaffoldState
import androidx.navigation.navArgument
$ADDITIONAL_IMPORTS

object $DESTINATION_NAME: $GENERATED_DESTINATION {

    override val route get() = "$COMPOSED_ROUTE"
    $NAV_ARGUMENTS$DEEP_LINKS
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState?
    ) {
        $CONTENT_FUNCTION_CODE
    }
    $WITH_ARGS_METHOD
}
""".trimIndent()