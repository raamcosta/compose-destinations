package com.ramcosta.composedestinations.navargs

import androidx.navigation.NavType

/**
 * [NavType] version for Compose Destinations library.
 * It gets used by the generated code.
 */
abstract class DestinationsNavType<T: Any?>: NavType<T>(true) {

    abstract fun serializeValue(value: T): String
}