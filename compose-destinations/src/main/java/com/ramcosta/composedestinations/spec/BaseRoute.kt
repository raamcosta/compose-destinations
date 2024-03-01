package com.ramcosta.composedestinations.spec

/**
 * Base class for all [Route]s.
 * It ensures correct equality methods to make [ExternalRoute]s
 * identity transparent.
 */
abstract class BaseRoute {

    final override fun equals(other: Any?): Boolean {
        return if (other is ExternalRoute) {
            this === other.original
        } else {
            this === other
        }
    }

    final override fun hashCode(): Int {
        return super.hashCode()
    }
}

/**
 * We want to make external routes essentially identity transparent,
 * so we use the identity related methods from [original].
 */
abstract class ExternalRoute {

    abstract val original: Route

    final override fun equals(other: Any?): Boolean {
        return original == other
    }

    final override fun hashCode(): Int {
        return original.hashCode()
    }
    final override fun toString(): String {
        return original.toString()
    }
}
