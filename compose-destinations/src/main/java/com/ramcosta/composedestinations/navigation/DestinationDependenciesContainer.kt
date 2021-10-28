package com.ramcosta.composedestinations.navigation

import kotlin.reflect.KClass

class DestinationDependenciesContainer: DependenciesContainerBuilder {

    private val _map: MutableMap<Class<*>, Any> = mutableMapOf()
    val map: Map<Class<*>, Any> = _map

    fun <T: Any> dependency(dependency: T, asType: KClass<in T>) {
        _map[asType.java] = dependency
    }

    inline fun <reified T: Any> get(): T {
        return map[T::class.java] as? T? ?: throw RuntimeException("${T::class.java.simpleName} was requested, but it is not present")
    }
}

sealed interface DependenciesContainerBuilder

inline fun <reified T: Any> DependenciesContainerBuilder.dependency(dependency: T) {
    (this as DestinationDependenciesContainer).dependency(dependency, asType = T::class)
}