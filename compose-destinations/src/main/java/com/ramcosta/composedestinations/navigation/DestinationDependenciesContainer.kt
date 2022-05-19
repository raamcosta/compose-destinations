package com.ramcosta.composedestinations.navigation

import com.ramcosta.composedestinations.dynamic.originalDestination
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.utils.navGraph
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
sealed interface DependenciesContainerBuilder<T>: DestinationScope<T>

/**
 * Adds [dependency] to this container builder.
 */
inline fun <reified D: Any, T> DependenciesContainerBuilder<T>.dependency(dependency: D) {
    (this as DestinationDependenciesContainer<*>).dependency(dependency, asType = D::class)
}

/**
 * Adds [dependencyProvider] return object to this container builder.
 * If [navGraph] is passed in, then only provides the dependency to destinations
 * that belongs to it.
 */
inline fun <reified D : Any, T> DependenciesContainerBuilder<T>.dependency(
    navGraph: NavGraphSpec,
    dependencyProvider: () -> D
) {
    if (navBackStackEntry.navGraph().route == navGraph.route) {
        (this as DestinationDependenciesContainer<*>).dependency(dependencyProvider(), asType = D::class)
    }
}

/**
 * Adds [dependencyProvider] return object to this container builder.
 * If [navGraph] is passed in, then only provides the dependency to destinations
 * that belongs to it.
 */
inline fun <reified D : Any, T> DependenciesContainerBuilder<T>.dependency(
    destination: DestinationSpec<*>,
    dependencyProvider: () -> D
) {
    if (this.destination.originalDestination.route == destination.originalDestination.route) {
        (this as DestinationDependenciesContainer<*>).dependency(dependencyProvider(), asType = D::class)
    }
}

/**
 * Container of all dependencies that can be used in a certain `Destination` Composable.
 * You can use `DestinationsNavHost` to add dependencies to it via
 * [DependenciesContainerBuilder.dependency]
 */
class DestinationDependenciesContainer<T>(
    destinationScope: DestinationScope<T>
): DependenciesContainerBuilder<T>, DestinationScope<T> by destinationScope {

    private val map: MutableMap<Class<*>, Any> = mutableMapOf()

    fun <D: Any> dependency(dependency: D, asType: KClass<in D>) {
        map[asType.java] = dependency
    }

    inline fun <reified D: Any> require(): D {
        return require(D::class)
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <D: Any> require(type: KClass<in D>): D {
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
            ?: throw RuntimeException("${type.java.simpleName} was requested, but it is not present")
    }
}