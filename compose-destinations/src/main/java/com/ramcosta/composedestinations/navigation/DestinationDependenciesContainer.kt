package com.ramcosta.composedestinations.navigation

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
sealed interface DependenciesContainerBuilder

/**
 * Adds [dependency] to this container builder. By default it will be associated with
 * its declared class, but you can force a super class explicitly like this:
 *
 * ```
 * val someComponentImplementation: SomeComponentImplementation = ...
 * builder.dependency<SomeComponentInterface>(someComponentImplementation)
 * ```
 */
inline fun <reified T: Any> DependenciesContainerBuilder.dependency(dependency: T) {
    (this as DestinationDependenciesContainer).dependency(dependency, asType = T::class)
}

/**
 * Container of all dependencies that can be used in a certain `Destination` Composable.
 * You can use generated `DestinationsNavHost` to add dependencies to it via
 * [DependenciesContainerBuilder.dependency]
 */
class DestinationDependenciesContainer: DependenciesContainerBuilder {

    private val _map: MutableMap<Class<*>, Any> = mutableMapOf()
    val map: Map<Class<*>, Any> = _map

    fun <T: Any> dependency(dependency: T, asType: KClass<in T>) {
        _map[asType.java] = dependency
    }

    inline fun <reified T: Any> require(): T {
        return map[T::class.java] as? T? ?: throw RuntimeException("${T::class.java.simpleName} was requested, but it is not present")
    }
}