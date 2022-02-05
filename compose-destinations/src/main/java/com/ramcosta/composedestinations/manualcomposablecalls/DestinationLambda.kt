package com.ramcosta.composedestinations.manualcomposablecalls

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable

@SuppressLint("ComposableNaming")
sealed class DestinationLambda<T> {

    @Composable
    abstract operator fun invoke(
        destinationScope: DestinationScope<T>
    )

    class Normal<T>(
        val content: @Composable DestinationScope<T>.() -> Unit
    ) : DestinationLambda<T>() {

        @Composable
        override operator fun invoke(
            destinationScope: DestinationScope<T>
        ) {
            destinationScope.content()
        }
    }

    @ExperimentalAnimationApi
    class Animated<T>(
        val content: @Composable AnimatedDestinationScope<T>.() -> Unit
    ) : DestinationLambda<T>() {

        @Composable
        override operator fun invoke(
            destinationScope: DestinationScope<T>
        ) {
            (destinationScope as AnimatedDestinationScope<T>).content()
        }
    }

    class BottomSheet<T>(
        val content: @Composable BottomSheetDestinationScope<T>.() -> Unit
    ) : DestinationLambda<T>() {

        @Composable
        override operator fun invoke(
            destinationScope: DestinationScope<T>
        ) {
            (destinationScope as BottomSheetDestinationScope<T>).content()
        }
    }
}