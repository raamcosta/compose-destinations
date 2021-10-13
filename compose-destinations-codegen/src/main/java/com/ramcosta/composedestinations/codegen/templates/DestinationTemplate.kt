package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME

//region anchors
const val ADDITIONAL_IMPORTS = "[ADDITIONAL_IMPORTS]"
const val DESTINATION_NAME = "[DESTINATION_NAME]"
const val COMPOSED_ROUTE = "[COMPOSED_ROUTE]"
const val NAV_ARGUMENTS = "[NAV_ARGUMENTS]"
const val DEEP_LINKS = "[DEEP_LINKS]"
const val DESTINATION_STYLE = "[DESTINATION_STYLE]"
const val WITH_ARGS_METHOD = "[WITH_ARGS_METHOD]"
const val CONTENT_FUNCTION_CODE = "[CONTENT_FUNCTION_CODE]"
const val REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER = "[REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER]"
//endregion

val destinationTemplate="""
package $PACKAGE_NAME

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
$ADDITIONAL_IMPORTS

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}object $DESTINATION_NAME: $GENERATED_DESTINATION {

    override val route = "$COMPOSED_ROUTE"
    $NAV_ARGUMENTS$DEEP_LINKS$DESTINATION_STYLE
    @Composable
    override fun Content(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        situationalParameters: Map<Class<*>, Any>,
    ) {
        $CONTENT_FUNCTION_CODE
    }
    $WITH_ARGS_METHOD
}
""".trimIndent()