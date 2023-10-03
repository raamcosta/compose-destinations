package com.ramcosta.composedestinations.dynamic.navgraph

import com.ramcosta.composedestinations.annotation.InternalDestinationsApi
import com.ramcosta.composedestinations.spec.TypedNavGraphSpec

@InternalDestinationsApi
interface DynamicNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS>: TypedNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS> {
    val originalNavGraph: TypedNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS>
}

@OptIn(InternalDestinationsApi::class)
@PublishedApi
internal val <NAV_ARGS, START_ROUTE_NAV_ARGS> TypedNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS>.originalNavGraph
    get(): TypedNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS> =
        if (this is DynamicNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS>) {
            this.originalNavGraph
        } else {
            this
        }
