package com.ramcosta.composedestinations.navigation

import com.ramcosta.composedestinations.manualcomposablecalls.DestinationScope
import kotlin.reflect.KClass

/**
 * Interface through which you can add dependencies to a [DestinationDependenciesContainer].
 * Use [dependency] method to do that.
 * `DestinationsNavHost` has a lambda called for each destination with an instance of this interface
 * which lets you add dependencies to it.
 *
 * Each dependency added is associated with a given class (reified in [dependency]). The calling
 * `Destination` can then declare arguments of those class types.
 */
sealed interface DependenciesContainerBuilder<T>: DestinationScope<T>

/**
 * Adds [dependency] to this container builder. By default it will be associated with
 * its declared class, but you can force a super class explicitly like this:
 *
 * ```
 * val someComponentImplementation: SomeComponentImplementation = ...
 * builder.dependency<SomeComponentInterface>(someComponentImplementation)
 * ```
 */
inline fun <reified D: Any, T> DependenciesContainerBuilder<T>.dependency(dependency: D) {
    (this as DestinationDependenciesContainer<*>).dependency(dependency, asType = D::class)
}

/**
 * Container of all dependencies that can be used in a certain `Destination` Composable.
 * You can use generated `DestinationsNavHost` to add dependencies to it via
 * [DependenciesContainerBuilder.dependency]
 */
class DestinationDependenciesContainer<T>(
    destinationScope: DestinationScope<T>
): DependenciesContainerBuilder<T>, DestinationScope<T> by destinationScope {

    private val _map: MutableMap<Class<*>, Any> = mutableMapOf()
    private val map: Map<Class<*>, Any> = _map

    fun <D: Any> dependency(dependency: D, asType: KClass<in D>) {
        _map[asType.java] = dependency
    }

    inline fun <reified D: Any> require(): D {
        return require(D::class)
    }

    @Suppress("UNCHECKED_CAST")
    fun <D: Any> require(type: KClass<in D>): D {
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