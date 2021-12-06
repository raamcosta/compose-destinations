package com.ramcosta.composedestinations.navigation

import kotlin.reflect.KClass

/**
 * Container of all dependencies that can be used in a certain `Destination` Composable.
 */
class DestinationDependenciesContainer {

    private val _map: MutableMap<Class<*>, Any> = mutableMapOf()
    val map: Map<Class<*>, Any> = _map

    fun <T: Any> dependency(dependency: T, asType: KClass<in T>) {
        _map[asType.java] = dependency
    }

    inline fun <reified T: Any> require(): T {
        return map[T::class.java] as? T? ?: throw RuntimeException("${T::class.java.simpleName} was requested, but it is not present")
    }
}

inline fun <reified T: Any> DestinationDependenciesContainer.dependency(dependency: T) {
    dependency(dependency, asType = T::class)
}