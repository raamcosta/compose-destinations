package com.ramcosta.composedestinations.navargs.parcelable

import android.os.Parcelable
import androidx.navigation.NavType

/**
 * [NavType] version for Compose Destinations library.
 * It gets used by the generated code.
 */
abstract class DestinationsNavType<T: Parcelable?>: NavType<T>(true) {

    abstract fun serializeValue(value: Parcelable): String
}