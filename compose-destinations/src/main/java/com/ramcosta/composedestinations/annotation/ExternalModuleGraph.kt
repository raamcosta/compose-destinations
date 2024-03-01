package com.ramcosta.composedestinations.annotation

/**
 * Marks the [Destination] or [NavGraph] as having a parent navigation graph
 * defined in another module.
 * This will make the destination / nav graph not usable unless it is imported using
 * [ExternalDestination] or [ExternalNavGraph].
 */
@Retention(AnnotationRetention.SOURCE)
annotation class ExternalModuleGraph