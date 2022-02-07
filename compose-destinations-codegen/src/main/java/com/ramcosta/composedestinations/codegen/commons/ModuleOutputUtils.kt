package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.GeneratedDestination

fun startingDestination(
    navGraphRoute: String,
    generatedDestinations: List<GeneratedDestination>
): String {
    val startingDestinations = generatedDestinations.filter { it.isStartDestination }
    if (startingDestinations.isEmpty()) {
        throw IllegalDestinationsSetup("Use argument `start = true` in the @Destination annotation of the '$navGraphRoute' nav graph's start destination!")
    }

    if (startingDestinations.size > 1) {
        throw IllegalDestinationsSetup("Found ${startingDestinations.size} start destinations in '$navGraphRoute' nav graph, only one is allowed!")
    }

    return startingDestinations[0].simpleName
}

fun sourceIds(generatedDestinations: List<GeneratedDestination>): MutableList<String> {
    val sourceIds = mutableListOf<String>()
    generatedDestinations.forEach {
        sourceIds.addAll(it.sourceIds)
    }
    return sourceIds
}