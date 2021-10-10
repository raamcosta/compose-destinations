package com.ramcosta.composedestinations.codegen.templates

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

@ExperimentalMaterialNavigationApi
fun NavGraphBuilder.addBottomSheetComposable(
    destination: $GENERATED_DESTINATION,
    navController: NavHostController,
    situationalParametersProvider: ($GENERATED_DESTINATION) -> MutableMap<Class<*>, Any>
) {
    bottomSheet(
        destination.route,
        destination.arguments,
        destination.deepLinks
    ) { navBackStackEntry ->
        destination.Content(
            navController,
            navBackStackEntry,
            situationalParametersProvider(destination).apply {
                this[ColumnScope::class.java] = this@bottomSheet
            }
        )
    }
}

@ExperimentalMaterialNavigationApi
@Composable
fun BottomSheetLayout(
    navController: NavHostController,
    params: @Composable() (BottomSheetLayoutParams.Builder.() -> Unit),
    content: @Composable () -> Unit
) = with(BottomSheetLayoutParams.Builder().run { params(); build() }) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator

    ModalBottomSheetLayout(
        bottomSheetNavigator,
        modifier,
        sheetShape,
        sheetElevation,
        sheetBackgroundColor,
        sheetContentColor,
        scrimColor
    ) {
        content()
    }
}

class BottomSheetLayoutParams private constructor(
    val modifier: Modifier,
    val sheetElevation: Dp,
    val sheetShape: Shape,
    val sheetBackgroundColor: Color,
    val sheetContentColor: Color,
    val scrimColor: Color,
) {
    
    class Builder {
        private var modifier : Modifier? = null
        private var sheetElevation : Dp? = null
        private var sheetShape : Shape? = null
        private var sheetBackgroundColor : Color? = null
        private var sheetContentColor : Color? = null
        private var scrimColor : Color? = null

        fun modifier(modifier: Modifier) = apply { this.modifier = modifier }
       
        fun sheetElevation(dp: Dp) = apply { sheetElevation = dp }
        
        fun sheetShape(shape: Shape) = apply { sheetShape = shape }
        
        fun sheetBackgroundColor(color: Color) = apply { sheetBackgroundColor = color }
        
        fun sheetContentColor(color: Color) = apply { sheetContentColor = color }
        
        fun scrimColor(color: Color) = apply { scrimColor = color }
        
        @Composable
        fun build(): BottomSheetLayoutParams {
            val actualSheetBackgroundColor = sheetBackgroundColor ?: MaterialTheme.colors.surface
            
            return BottomSheetLayoutParams(
                modifier ?: Modifier,
                sheetElevation ?: ModalBottomSheetDefaults.Elevation,
                sheetShape ?: MaterialTheme.shapes.large,
                actualSheetBackgroundColor,
                sheetContentColor ?: contentColorFor(actualSheetBackgroundColor),
                scrimColor ?: ModalBottomSheetDefaults.scrimColor,
            )
        }
    }
}
""".trimIndent()