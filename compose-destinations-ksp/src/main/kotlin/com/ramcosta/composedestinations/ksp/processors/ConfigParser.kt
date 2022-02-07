@file:Suppress("SameParameterValue")

package com.ramcosta.composedestinations.ksp.processors

import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenMode

class ConfigParser(
    private val options: Map<String, String>
) {

    companion object {
        private const val PREFIX = "compose-destinations"

        // Configs
        private const val MODULE_NAME = "$PREFIX.moduleName"
        private const val GEN_PACKAGE_NAME = "$PREFIX.codeGenPackageName"
        private const val MODE = "$PREFIX.mode"

        // Mode options
        private const val MODE_NAV_GRAPHS = "navgraphs"
        private const val MODE_DESTINATIONS = "destinations"
        private const val MODE_SINGLE_MODULE = "singlemodule"
    }

    fun parse(): CodeGenConfig {
        val packageName = options[GEN_PACKAGE_NAME]?.trim()?.removeSuffix(".")
        val moduleName = options[MODULE_NAME]?.trim()?.filter { it.isLetter() }
        val mode = parseMode(MODE)

        return CodeGenConfig(
            moduleName = moduleName,
            mode = mode,
            packageName = packageName
        )
    }

    private fun parseMode(key: String): CodeGenMode {
        val option = options[key]

        if (option != null) {
            return when (option) {
                MODE_DESTINATIONS -> CodeGenMode.Destinations

                MODE_NAV_GRAPHS -> CodeGenMode.NavGraphs

                MODE_SINGLE_MODULE -> CodeGenMode.SingleModule

                else -> throw WrongConfigurationSetup(message = "$key has wrong value! It has to be one of: " +
                            "'$MODE_NAV_GRAPHS', '$MODE_DESTINATIONS', '$MODE_SINGLE_MODULE'"
                )
            }
        }

        return CodeGenMode.SingleModule
    }
}

class WrongConfigurationSetup(message: String, cause: Throwable? = null) : RuntimeException(message, cause)