package com.ramcosta.composedestinations.navigation

import com.ramcosta.composedestinations.dynamic.originalDestination
import com.ramcosta.composedestinations.scope.DestinationScope
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
sealed interface DependenciesContainerBuilder<T> : DestinationScope<T>

sealed interface DestinationDependenciesContainer

inline fun <reified D : Any> DestinationDependenciesContainer.require(
    isMarkedNavHostParam: Boolean = false,
): D {
    return (this as DestinationDependenciesContainerImpl<*>).require(isMarkedNavHostParam)
}

/**
 * Adds [dependency] to this container builder.
 */
inline fun <reified D : Any, T> DependenciesContainerBuilder<T>.dependency(dependency: D) {
    (this as DestinationDependenciesContainerImpl<*>).dependency(dependency, asType = D::class)
}

/**
 * Adds [dependencyProvider] return object to this container builder.
 * If [navGraph] is passed in, then only provides the dependency to destinations
 * that belongs to it.
 */
inline fun <reified D : Any, T> DependenciesContainerBuilder<T>.dependency(
    navGraph: NavGraphSpec,
    dependencyProvider: () -> D,
) {
    val route = requireNotNull(navBackStackEntry.destination.route)

    if (navGraph.findDestination(route) != null) {
        (this as DestinationDependenciesContainerImpl<*>).dependency(dependencyProvider(),
            asType = D::class)
    }
}

/**
 * Adds [dependencyProvider] return object to this container builder.
 * If [destination] is passed in, then only provides the dependency in case that is the
 * destination being navigated to.
 */
inline fun <reified D : Any, T> DependenciesContainerBuilder<T>.dependency(
    destination: DestinationSpec<*>,
    dependencyProvider: () -> D,
) {
    if (this.destination.originalDestination.route == destination.originalDestination.route) {
        (this as DestinationDependenciesContainerImpl<*>).dependency(dependencyProvider(),
            asType = D::class)
    }
}

/**
 * Container of all dependencies that can be used in a certain `Destination` Composable.
 * You can use `DestinationsNavHost` to add dependencies to it via
 * [DependenciesContainerBuilder.dependency]
 */
@PublishedApi
internal class DestinationDependenciesContainerImpl<T>(
    destinationScope: DestinationScope<T>,
) : DestinationDependenciesContainer,
    DependenciesContainerBuilder<T>,
    DestinationScope<T> by destinationScope {

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