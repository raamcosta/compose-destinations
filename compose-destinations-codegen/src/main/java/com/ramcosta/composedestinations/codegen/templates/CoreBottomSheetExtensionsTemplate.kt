package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.COLUMN_SCOPE_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME

val coreBottomSheetExtensionsTemplate = """
package $PACKAGE_NAME    

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import $PACKAGE_NAME.spec.DestinationStyle
import $PACKAGE_NAME.navigation.DependenciesContainerBuilder

@ExperimentalMaterialNavigationApi
fun NavGraphBuilder.addBottomSheetComposable(
    destination: $GENERATED_DESTINATION,
    navController: NavHostController,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.($GENERATED_DESTINATION) -> Unit
) {
    bottomSheet(
        destination.route,
        destination.arguments,
        destination.deepLinks
    ) { navBackStackEntry ->
        destination.Content(
            navController,
            navBackStackEntry,
            {
                add(this@bottomSheet, asType = $COLUMN_SCOPE_SIMPLE_NAME::class.java)
                dependenciesContainerBuilder.invoke(this, destination)
            }
        )
    }
}
""".trimIndent()