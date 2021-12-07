package com.ramcosta.composedestinations

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationSpec

sealed class DestinationLambda(val noArgs: Boolean) {

    @Composable
    abstract operator fun invoke(
        destination: DestinationSpec<*>,
        navBackStackEntry: NavBackStackEntry,
        receiver: Any?,
    )

    class Normal<T>(
        val content: @Composable (T, NavBackStackEntry) -> Unit,
        noArgs: Boolean = false
    ) : DestinationLambda(noArgs) {

        @Suppress("UNCHECKED_CAST")
        @Composable
        override operator fun invoke(
            destination: DestinationSpec<*>,
            navBackStackEntry: NavBackStackEntry,
            receiver: Any?
        ) {
            if (noArgs) {
                this as Normal<Unit>
                content(
                    Unit,
                    navBackStackEntry
                )
            } else {
                destination as DestinationSpec<T>
                content(
                    remember { destination.argsFrom(navBackStackEntry) },
                    navBackStackEntry
                )
            }
        }
    }

    @ExperimentalAnimationApi
    class Animated<T>(
        val content: @Composable AnimatedVisibilityScope.(T, NavBackStackEntry) -> Unit,
        noArgs: Boolean = false
    ) : DestinationLambda(noArgs) {

        @Suppress("UNCHECKED_CAST")
        @Composable
        override operator fun invoke(
            destination: DestinationSpec<*>,
            navBackStackEntry: NavBackStackEntry,
            receiver: Any?
        ) {
            if (noArgs) {
                this as Animated<Unit>
                (receiver as AnimatedVisibilityScope).content(
                    Unit,
                    navBackStackEntry
                )
            } else {
                destination as DestinationSpec<T>
                (receiver as AnimatedVisibilityScope).content(
                    remember { destination.argsFrom(navBackStackEntry) },
                    navBackStackEntry
                )
            }
        }
    }

    class BottomSheet<T>(
        val content: @Composable ColumnScope.(T, NavBackStackEntry) -> Unit,
        noArgs: Boolean = false
    ) : DestinationLambda(noArgs) {

        @Suppress("UNCHECKED_CAST")
        @Composable
        override operator fun invoke(
            destination: DestinationSpec<*>,
            navBackStackEntry: NavBackStackEntry,
            receiver: Any?
        ) {
            if (noArgs) {
                this as BottomSheet<Unit>
                (receiver as ColumnScope).content(
                    Unit,
                    navBackStackEntry
                )
            } else {
                destination as DestinationSpec<T>
                (receiver as ColumnScope).content(
                    remember { destination.argsFrom(navBackStackEntry) },
                    navBackStackEntry
                )
            }
        }
    }
}