package com.ramcosta.composedestinations.manualcomposablecalls

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.DestinationSpec

@SuppressLint("ComposableNaming")
sealed class DestinationLambda<T> {

    @Composable
    abstract operator fun invoke(
        destination: DestinationSpec<T>,
        navBackStackEntry: NavBackStackEntry,
        navController: NavController,
        receiver: Any?,
    )

    class Normal<T>(
        val content: @Composable DestinationScope<T>.() -> Unit
    ) : DestinationLambda<T>() {

        @Composable
        override operator fun invoke(
            destination: DestinationSpec<T>,
            navBackStackEntry: NavBackStackEntry,
            navController: NavController,
            receiver: Any?
        ) {
            val scope = remember {
                DestinationScopeImpl(
                    destination,
                    navBackStackEntry,
                    navController
                )
            }

            scope.content()
        }
    }

    @ExperimentalAnimationApi
    class Animated<T>(
        val content: @Composable AnimatedDestinationScope<T>.() -> Unit
    ) : DestinationLambda<T>() {

        @Composable
        override operator fun invoke(
            destination: DestinationSpec<T>,
            navBackStackEntry: NavBackStackEntry,
            navController: NavController,
            receiver: Any?
        ) {
            val scope = remember {
                AnimatedDestinationScopeImpl(
                    destination,
                    navBackStackEntry,
                    navController,
                    (receiver as AnimatedVisibilityScope),
                )
            }

            scope.content()
        }
    }

    class BottomSheet<T>(
        val content: @Composable BottomSheetDestinationScope<T>.() -> Unit
    ) : DestinationLambda<T>() {

        @Composable
        override operator fun invoke(
            destination: DestinationSpec<T>,
            navBackStackEntry: NavBackStackEntry,
            navController: NavController,
            receiver: Any?
        ) {
            val scope = remember {
                BottomSheetDestinationScopeImpl(
                    destination,
                    navBackStackEntry,
                    navController,
                    (receiver as ColumnScope),
                )
            }

            scope.content()
        }
    }
}