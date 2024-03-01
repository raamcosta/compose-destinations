package com.ramcosta.composedestinations.spec

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.scope.DestinationScope

/**
 * Used by generated code when there's a module that only outputs destinations
 * and no nav graphs.
 * Generated classes can be used to import all destinations with a single
 * [com.ramcosta.composedestinations.annotation.ExternalDestination] annotation.
 */
interface ModuleDestinationsContainer : DirectionDestinationSpec {

    val destinations: List<DestinationSpec>

    @Composable
    override fun DestinationScope<Unit>.Content() {
        error("Do not use 'ModuleDestinationsContainer', it is meant for generated code only!")
    }

    override val route: String
        get() = error("Do not use 'ModuleDestinationsContainer', it is meant for generated code only!")

    override val baseRoute: String
        get() = error("Do not use 'ModuleDestinationsContainer', it is meant for generated code only!")
}