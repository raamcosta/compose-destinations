package com.ramcosta.composedestinations.navargs.serializable

import androidx.navigation.NavType
import java.io.Serializable

/**
 * [Serializable] [NavType] version for Compose Destinations library.
 * It gets used by the generated code.
 */
abstract class SerializableDestinationsNavType<T: Serializable?>: NavType<T>(true) {

    abstract fun serializeValue(value: Serializable, isMandatoryArg: Boolean): String
}