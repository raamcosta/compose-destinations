package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import kotlin.reflect.KClass

/**
 * Marks a `Composable` function as a navigation graph destination.
 * A `Destination` will be generated for each of these which will include
 * the full route, the nav arguments and the `Composable` function which
 * will call the annotated one, when the destination gets navigated to.
 *
 * Can also be used on another annotation class which can then itself be
 * used in the `Composable` function (or another annotation class, recursively).
 * If used this way, it will carry over its values, allowing for easy single place
 * where you define Wrappers or DeepLinks for example.
 *
 * @param route main route of this destination (by default, the name of the Composable function)
 * @param navArgsDelegate class with a primary constructor where all navigation arguments are
 * to be defined. Useful when the arguments are not needed in this Composable or to simplify
 * the Composable function signature when it has a lot of navigation arguments (which should be rare).
 * The generated `Destination` class has `argsFrom` methods that accept a `NavBackStackEntry`
 * or a `SavedStateHandle` (useful inside a ViewModel) and return an instance of this class.
 * @param deepLinks array of [DeepLink] which can be used to navigate to this destination
 * @param style class of a [DestinationStyle] subclass which is used to define the destination style:
 * its transitions animations OR if it is dialog destination OR a bottom sheet destination. For
 * bottom sheet, you need to use the "io.github.raamcosta.compose-destinations:bottom-sheet"
 * dependency.
 * @param wrappers array of [DestinationWrapper]s with which to wrap the destination screen with.
 * Note that the order is relevant, as it is the same order the wrappers will be called in.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Destination(
    val route: String = COMPOSABLE_NAME,
    val navArgsDelegate: KClass<*> = Nothing::class,
    val deepLinks: Array<DeepLink> = [],
    val style: KClass<out DestinationStyle> = DestinationStyle.Default::class,
    val wrappers: Array<KClass<out DestinationWrapper>> = [],
) {
    companion object {
        const val COMPOSABLE_NAME = "@ramcosta.destinations.composable-name-route@"
    }
}
