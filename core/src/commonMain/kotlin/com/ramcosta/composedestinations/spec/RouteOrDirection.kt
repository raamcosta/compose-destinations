package com.ramcosta.composedestinations.spec

/**
 * [Route] or [Direction]
 */
sealed interface RouteOrDirection {
    val route: String
}