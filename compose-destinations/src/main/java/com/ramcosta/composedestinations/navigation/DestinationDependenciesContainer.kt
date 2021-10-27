package com.ramcosta.composedestinations.navigation

interface DependenciesContainerBuilder {
    fun <T: Any> add(dependency: T, asType: Class<in T> = dependency.javaClass)

}

class DestinationDependenciesContainer: DependenciesContainerBuilder {

    private val _map: MutableMap<Class<*>, Any> = mutableMapOf()
    val map: Map<Class<*>, Any> = _map

    override fun <T: Any> add(dependency: T, asType: Class<in T>) {
        _map[asType] = dependency
    }

    inline fun <reified T: Any> get(): T {
        return map[T::class.java] as? T? ?: throw RuntimeException("${T::class.java.simpleName} was requested, but not present")
    }
}