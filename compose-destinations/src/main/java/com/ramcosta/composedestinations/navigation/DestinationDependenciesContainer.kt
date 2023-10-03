package com.ramcosta.composedestinations.navigation

import com.ramcosta.composedestinations.dynamic.destination.originalDestination
import com.ramcosta.composedestinations.scope.DestinationScopeWithNoDependencies
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.utils.findDestination
import kotlin.reflect.KClass

/**
 * Interface through which you can add dependencies to a [DestinationDependenciesContainer].
 * Use [dependency] methods to do that.
 * `DestinationsNavHost` has a lambda called for each destination with an instance of this interface
 * which lets you add dependencies to it.
 *
 * Each dependency added is associated with a given class (reified in [dependency]). The calling
 * `Destination` can then declare arguments of those class types or super types.
 */
sealed interface DependenciesContainerBuilder<T> : DestinationScopeWithNoDependencies<T>

/**
 * Adds [dependency] to this container builder.
 */
inline fun <reified D : Any, T> DependenciesContainerBuilder<T>.dependency(dependency: D) {
    (this as DestinationDependenciesContainerImpl<*>).dependency(dependency, asType = D::class)
}

/**
 * Calls [dependencyProvider] when a destination (directly or indirectly) part of [navGraph] is
 * navigated to, giving an opportunity to specify dependencies with [dependency] method
 * that are supposed to be used only by destinations of that [navGraph].
 */
inline fun <T> DependenciesContainerBuilder<T>.navGraph(
    navGraph: NavGraphSpec,
    dependencyProvider: DependenciesContainerBuilder<T>.() -> Unit,
) {
    val route = requireNotNull(navBackStackEntry.destination.route)

    if (navGraph.findDestination(route) != null) {
        dependencyProvider()
    }
}

/**
 * Calls [dependencyProvider] when a [destination] is navigated to,
 * giving an opportunity to specify dependencies with [dependency] method
 * that are supposed to be used only by that [destination].
 */
inline fun <T> DependenciesContainerBuilder<T>.destination(
    destination: DestinationSpec,
    dependencyProvider: DependenciesContainerBuilder<T>.() -> Unit,
) {
    if (this.destination.originalDestination.route == destination.originalDestination.route) {
        dependencyProvider()
    }
}

/**
 *
 * Container of all dependencies that can be used in a certain `Destination` Composable.
 * You can use `DestinationsNavHost` to add dependencies to it via
 * [DependenciesContainerBuilder.dependency]
 *
 * Helpful:
 * - Internally by generated code to get dependencies your annotated Composables
 * require.
 * - When using [com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder]
 * you can get a hold of it by calling [com.ramcosta.composedestinations.scope.DestinationScope.buildDependencies].
 * - When using [com.ramcosta.composedestinations.wrapper.DestinationWrapper] feature you'll also be given
 * a [com.ramcosta.composedestinations.scope.DestinationScope] where you can get this by its `dependencies`
 * method.
 */
sealed interface DestinationDependenciesContainer

/**
 * Function through which you can get a hold of the dependencies inside [DestinationDependenciesContainer].
 *
 * @param isMarkedNavHostParam is used internally by generated code only to give a helping error in case
 * the dependency is missing. You can always use the default value here.
 */
inline fun <reified D : Any> DestinationDependenciesContainer.require(
    isMarkedNavHostParam: Boolean = false,
): D {
    return (this as DestinationDependenciesContainerImpl<*>).require(isMarkedNavHostParam)
}

@PublishedApi
internal class DestinationDependenciesContainerImpl<T>(
    destinationScope: DestinationScopeWithNoDependencies<T>,
) : DestinationDependenciesContainer,
    DependenciesContainerBuilder<T>,
    DestinationScopeWithNoDependencies<T> by destinationScope {

    private val map: MutableMap<Class<*>, Any> = mutableMapOf()

    fun <D : Any> dependency(dependency: D, asType: KClass<in D>) {
        map[asType.java] = dependency
    }

    inline fun <reified D : Any> require(
        isMarkedNavHostParam: Boolean = false,
    ): D {
        return require(D::class, isMarkedNavHostParam)
    }

    @Suppress("UNCHECKED_CAST")
    fun <D : Any> require(type: KClass<in D>, isMarkedNavHostParam: Boolean): D {
        var depForType: D? = map[type.java] as? D

        if (depForType == null) {
            depForType = map.values.firstOrNull {
                type.java.isAssignableFrom(it.javaClass)
            } as? D

            depForType?.let {
                // Cache for next compositions
                dependency(it, type)
            }
        }

        return depForType
            ?: throw RuntimeException(
                if (isMarkedNavHostParam) {
                    "${type.java.simpleName} was requested and it is marked with @NavHostParam but it " +
                            "was not provided via dependency container"
                } else {
                    "${type.java.simpleName} was requested, but it is not present"
                }
            )
    }
}