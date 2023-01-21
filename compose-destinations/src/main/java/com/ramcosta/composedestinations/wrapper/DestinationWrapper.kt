package com.ramcosta.composedestinations.wrapper

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.scope.DestinationScope

interface DestinationWrapper {

    @Composable
    fun <T> DestinationScope<T>.Wrap(
        screenContent: @Composable () -> Unit
    )
}

@Composable
fun DestinationScope<*>.Wrap(
    wrapper: DestinationWrapper,
    content: @Composable () -> Unit
) {
    with(wrapper) {
        Wrap {
            content()
        }
    }
}

@Composable
fun DestinationScope<*>.WrapRecursively(
    wrappers: Array<DestinationWrapper>,
    idx: Int = 0,
    content: @Composable () -> Unit
) {
    with(wrappers[idx]) {
        Wrap {
            if (idx < wrappers.lastIndex) {
                WrapRecursively(wrappers, idx + 1, content)
            } else {
                content()
            }
        }
    }
}
