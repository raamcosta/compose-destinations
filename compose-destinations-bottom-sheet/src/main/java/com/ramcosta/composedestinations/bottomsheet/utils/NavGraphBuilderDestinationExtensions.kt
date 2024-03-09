package com.ramcosta.composedestinations.bottomsheet.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.ramcosta.composedestinations.bottomsheet.scope.BottomSheetNavGraphBuilderDestinationScopeImpl
import com.ramcosta.composedestinations.bottomsheet.spec.DestinationStyleBottomSheet
import com.ramcosta.composedestinations.scope.BottomSheetNavGraphBuilderDestinationScope
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

/**
 * Like [com.google.accompanist.navigation.material.bottomSheet] but accepts
 * a [DestinationSpec] to get the route, arguments and deep links.
 *
 * The [content] lambda will receive the navigation arguments class ([T]).
 *
 * Useful if you opt to use [com.google.accompanist.navigation.animation.AnimatedNavHost] instead of
 * [com.ramcosta.composedestinations.DestinationsNavHost].
 * This way, you can build the navigation graph in the "vanilla compose navigation" way.
 * If you do this, you should also disable the `NavGraphs` generation
 * in build.gradle:
 * ```
 * ksp {
 *     arg("compose-destinations.generateNavGraphs", "false")
 * }
 * ```
 */
@ExperimentalMaterialNavigationApi
fun <T> NavGraphBuilder.bottomSheetComposable(
    destination: TypedDestinationSpec<T>,
    content: @Composable BottomSheetNavGraphBuilderDestinationScope<T>.() -> Unit
) {
    when (destination.style) {
        is DestinationStyleBottomSheet -> {
            bottomSheet(
                destination.route,
                destination.arguments,
                destination.deepLinks,
            ) {
                val scope = remember(destination, it, this) {
                    BottomSheetNavGraphBuilderDestinationScopeImpl(
                        destination,
                        it,
                        this
                    )
                }
                scope.content()
            }
        }
        is DestinationStyle.Dialog -> {
            throw IllegalArgumentException("You need to use `dialogComposable` for Dialog destinations!")
        }
        else -> {
            throw IllegalArgumentException("You need to use `composable` for Animated or Default styled destinations!")
        }
    }
}