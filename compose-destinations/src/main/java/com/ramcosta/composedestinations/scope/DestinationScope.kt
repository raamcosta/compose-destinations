package com.ramcosta.composedestinations.scope

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DestinationsBooleanNavType
import com.ramcosta.composedestinations.navargs.primitives.DestinationsFloatNavType
import com.ramcosta.composedestinations.navargs.primitives.DestinationsIntNavType
import com.ramcosta.composedestinations.navargs.primitives.DestinationsLongNavType
import com.ramcosta.composedestinations.navargs.primitives.DestinationsStringNavType
import com.ramcosta.composedestinations.navigation.DestinationDependenciesContainer
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.resultBackNavigator
import com.ramcosta.composedestinations.result.resultRecipient
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.TypedDestinationSpec
import kotlin.reflect.KClass

/**
 * Scope where a destination screen will be called in.
 */
@Immutable
interface DestinationScope<T>: DestinationScopeWithNoDependencies<T> {

    /**
     * [DestinationSpec] related to this scope
     */
    override val destination: TypedDestinationSpec<T>

    /**
     * [NavBackStackEntry] of the current destination
     */
    override val navBackStackEntry: NavBackStackEntry

    /**
     * [NavController] related to the NavHost
     */
    override val navController: NavController

    /**
     * [DestinationsNavigator] useful to navigate from this destination
     */
    override val destinationsNavigator: DestinationsNavigator

    /**
     * Builds the [DestinationDependenciesContainer] which contains
     * all dependencies by calling `DestinationsNavHost`'s `dependencyContainerBuilder` lambda parameter.
     *
     * When used, it will run the `dependencyContainerBuilder` so even though that lambda
     * is not expected to do any heavy calculations, use it only once per composition. So if you
     * need multiple dependencies, store the result of this in a val first, then use the val each time.
     */
    @Composable
    fun buildDependencies(): DestinationDependenciesContainer

    /**
     * Class holding the navigation arguments passed to this destination
     * or [Unit] if the destination has no arguments
     */
    override val navArgs: T
}

/**
 * Returns a well typed [ResultBackNavigator] for this [DestinationScope]
 *
 * When calling this directly (usually when using manually calling your destination
 * Composables), you can go into the corresponding generated Destination and check what
 * [DestinationsNavType] is passed in to the corresponding Composable [ResultBackNavigator]
 * parameter.
 *
 * @param resultNavType [DestinationsNavType] for the [R] type, which will
 * handle serialization of the result.
 */
@Composable
fun <R> DestinationScopeWithNoDependencies<*>.resultBackNavigator(
    resultNavType: DestinationsNavType<in R>
): ResultBackNavigator<R> =
    resultBackNavigator(destination, resultNavType, navController, navBackStackEntry)

/**
 * Returns a well typed [ResultRecipient] for this [DestinationScope]
 *
 * When calling this directly (usually when using manually calling your destination
 * Composables), you can go into the corresponding generated Destination and check what
 * [DestinationsNavType] is passed in to the corresponding Composable [ResultBackNavigator]
 * parameter.
 *
 * @param resultNavType [DestinationsNavType] for the [R] type, which will
 * handle serialization of the result.
 */
@Composable
inline fun <reified D : DestinationSpec, R> DestinationScopeWithNoDependencies<*>.resultRecipient(
    resultNavType: DestinationsNavType<in R>
): ResultRecipient<D, R> =
    resultRecipient(navBackStackEntry, D::class, resultNavType)

/**
 * Like [DestinationScope] but also [AnimatedVisibilityScope] so that
 * you can use this Scope as a receiver of your Animated or Default styled Composable
 */
@Immutable
interface AnimatedDestinationScope<T> : DestinationScope<T>, AnimatedVisibilityScope

/**
 * Like [DestinationScope] but also [ColumnScope] so that
 * if you're using the "bottom-sheet" dependency you can use this Scope as a receiver
 * of your Bottom Sheet styled Composable
 */
@Immutable
interface BottomSheetDestinationScope<T> : DestinationScope<T>, ColumnScope

//region deprecated
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    level = DeprecationLevel.ERROR,
    message = "\n" +
            "Use the `resultRecipient` version that takes in a `DestinationsNavType` parameter!\n" +
            "Example: for Boolean results, it's `booleanNavType`, for custom types it's `customTypeClassNameNavType`."
)
@Composable
inline fun <reified D : DestinationSpec, R, reified T : R & Any>
        DestinationScopeWithNoDependencies<*>.resultRecipient(): ResultRecipient<D, R> =
    resultRecipient(tryGettingNavType(T::class))

@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    level = DeprecationLevel.ERROR,
    message = "\n" +
            "Use the `resultBackNavigator` version that takes in a `DestinationsNavType` parameter!\n" +
            "Example: for Boolean results, it's `booleanNavType`, for custom types it's `customTypeClassNameNavType`."
)
@Composable
inline fun <R, reified T : R & Any> DestinationScopeWithNoDependencies<*>.resultBackNavigator(): ResultBackNavigator<R> =
    resultBackNavigator(tryGettingNavType(T::class))

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal fun <R, T : R & Any> tryGettingNavType(kClass: KClass<T>): DestinationsNavType<in R> {
    return when (kClass) {
        String::class -> DestinationsStringNavType
        Boolean::class -> DestinationsBooleanNavType
        Float::class -> DestinationsFloatNavType
        Int::class -> DestinationsIntNavType
        Long::class -> DestinationsLongNavType
        else -> error(
            "Use the `resultBackNavigator`/`resultRecipient` version that takes in a `DestinationsNavType` parameter!\n" +
                    "Example: for Boolean results, it's `booleanNavType`, for custom types it's `customTypeClassNameNavType`."
        )
    } as DestinationsNavType<in R>
}
//
