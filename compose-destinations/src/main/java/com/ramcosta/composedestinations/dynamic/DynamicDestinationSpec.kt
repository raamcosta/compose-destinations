package com.ramcosta.composedestinations.dynamic

import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.NavGraphSpec

interface DynamicDestinationSpec<T> : DestinationSpec<T> {
    val delegate: DestinationSpec<T>
}

infix fun <T> DestinationSpec<T>.routedIn(navGraph: NavGraphSpec): DestinationSpec<T> {
    return object: DynamicDestinationSpec<T>, DestinationSpec<T> by this {
        override val routeId = "${navGraph.route}/${this@routedIn.routeId}"

        override val route = "${navGraph.route}/${this@routedIn.route}"

        override val delegate = this@routedIn
    }
}

fun List<DestinationSpec<*>>.routedIn(spec: NavGraphSpec): List<DestinationSpec<*>> {
    return map { it routedIn spec }
}

infix fun Direction.within(navGraph: NavGraphSpec): Direction {
    return object: Direction by this@within {
        override val route = "${navGraph.route}/${this@within.route}"
    }
}