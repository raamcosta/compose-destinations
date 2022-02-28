package com.ramcosta.composedestinations.navargs.custom

import androidx.navigation.NavType
import java.io.Serializable

/**
 * [Any] [NavType] version for Compose Destinations library.
 * It gets used by the generated code.
 */
abstract class CustomDestinationsNavType<T: Any?>: NavType<T>(true) {

    abstract fun serializeValue(value: T): String?
}
