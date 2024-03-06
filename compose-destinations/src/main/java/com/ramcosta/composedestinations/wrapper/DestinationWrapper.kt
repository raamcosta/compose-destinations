package com.ramcosta.composedestinations.wrapper

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.scope.DestinationScope

/**
 * Interface you can implement with an object and then pass its class to
 * the [com.ramcosta.composedestinations.annotation.Destination.wrappers].
 *
 * It lets you extract common logic or UI from your screens out to a reusable piece
 * of code.
 *
 * Note that the wrapper can still access its own ViewModel or any other state holder
 * you need in its [Wrap] method.
 *
 * [DestinationScope] is [Wrap]s receiver and it allows you to get a hold of anything you
 * may need here including the destination the wrapper is wrapping at the any given time
 * ([DestinationScope.destination]), etc.
 *
 * USAGE EXAMPLE 1
 *
 * You can have multiple screens behind a PIN the user needs to enter.
 * Using this feature for this use case means:
 * - No need to change all of the affected screens' logic
 * - No need to navigate to a intermediate screen (which is specially annoying
 * if the final screen has navigation arguments).
 *
 * USAGE EXAMPLE 2
 *
 * You can also use it to display the state of the internet connection for example.
 * Just use a `Box` with a top right corner icon that shows up above the screen content
 * whenever the connectivity is lost.
 */
interface DestinationWrapper {

    /**
     * Method that will be called when the annotated Destination gets navigated to.
     *
     * @receiver [DestinationScope] which contains anything you may need at this point.
     * Like the destination currently being wrapped and its dependencies and arguments.
     *
     * @param screenContent is the Composable lambda which will call the real screen.
     * Make sure to call it, otherwise the screen will never show.
     */
    @Composable
    fun <T> DestinationScope<T>.Wrap(
        screenContent: @Composable () -> Unit
    )
}

//region used by generated code

/**
 * Function which will call [DestinationWrapper]'s Wrap method.
 *
 * Used by the generated code.
 */
@Composable
fun DestinationScope<*>.Wrap(
    wrapper: DestinationWrapper,
    content: @Composable () -> Unit
) {
    with(wrapper) {
        Wrap { content() }
    }
}

/**
 * Function which will call all [wrappers]' Wrap method.
 *
 * Used by the generated code.
 */
@Composable
fun DestinationScope<*>.Wrap(
    vararg wrappers: DestinationWrapper,
    content: @Composable () -> Unit
) {
    if (wrappers.isEmpty()) {
        content()
    } else {
        WrapRecursively(wrappers, 0, content)
    }
}

@Composable
private fun DestinationScope<*>.WrapRecursively(
    wrappers: Array<out DestinationWrapper>,
    idx: Int,
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

//endregion
