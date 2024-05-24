package com.ramcosta.composedestinations.spec

/**
 * Used by generated code when there's a module that only outputs destinations
 * and no nav graphs.
 * Generated classes can be used to import all destinations with a single
 * [com.ramcosta.composedestinations.annotation.ExternalModuleDestinations] annotation.
 */
interface ModuleDestinationsContainer {
    val destinations: List<DestinationSpec>
}
