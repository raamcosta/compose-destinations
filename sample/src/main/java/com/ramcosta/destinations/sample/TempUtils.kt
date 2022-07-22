package com.ramcosta.destinations.sample

import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route
import com.ramcosta.destinations.sample.destinations.DirectionDestination
import com.ramcosta.destinations.sample.destinations.TypedDestination
import java.io.InvalidObjectException

inline fun <reified T> Any.asRoute(): Route? {
    return when(T::class.java) {
        NavGraphSpec::class.java -> this as Route
        String::class.java -> NavGraphs.all.find { navGraph ->
            navGraph.route == this
        }
        else -> null
    }
}
