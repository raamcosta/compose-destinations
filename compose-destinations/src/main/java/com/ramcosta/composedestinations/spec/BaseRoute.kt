package com.ramcosta.composedestinations.spec

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
