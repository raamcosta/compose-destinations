@file:Suppress("SameParameterValue")

package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.processing.KSPLogger
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenMode

class ConfigParser(
    private val logger: KSPLogger,
    private val options: Map<String, String>
) {

    companion object {
        private const val PREFIX = "compose-destinations"

        // Configs
        private const val USE_COMPOSABLE_VISIBILITY = "$PREFIX.useComposableVisibility"
        private const val GEN_NAV_GRAPHS = "$PREFIX.generateNavGraphs"
        private const val GEN_PACKAGE_NAME = "$PREFIX.codeGenPackageName"
        private const val MODULE_NAME = "$PREFIX.moduleName"
        private const val MODE = "$PREFIX.mode"

        // Mode options
        private const val MODE_NAV_GRAPHS = "navgraphs"
        private const val MODE_DESTINATIONS = "destinations"
        private const val MODE_SINGLE_MODULE = "singlemodule"
    }

    fun parse(): CodeGenConfig {
        val packageName = options[GEN_PACKAGE_NAME]?.trim()?.removeSuffix(".")
        val moduleName = options[MODULE_NAME]?.trim()?.filter { it.isLetter() }
        val useComposableVisibility = parseBoolean(USE_COMPOSABLE_VISIBILITY) ?: false
        val mode = parseMode(MODE)

        return CodeGenConfig(
            moduleName = moduleName,
            mode = mode,
            packageName = packageName,
            useComposableVisibility = useComposableVisibility
        )
    }

    private fun parseMode(key: String): CodeGenMode {
        val option = options[key] ?: return singleModuleMode()

        return when (option) {
            MODE_DESTINATIONS -> {
                parseGenNavGraphsObjectConfig(false)
                CodeGenMode.Destinations
            }

            MODE_NAV_GRAPHS -> {
                parseGenNavGraphsObjectConfig(false)
                CodeGenMode.NavGraphs
            }

            MODE_SINGLE_MODULE -> singleModuleMode()

            else -> throw WrongConfigurationSetup(
                message = "$key has wrong value! It has to be one of: " +
                        "'$MODE_NAV_GRAPHS', '$MODE_DESTINATIONS', '$MODE_SINGLE_MODULE'"
            )
        }
    }

    private fun singleModuleMode(): CodeGenMode.SingleModule {
        return CodeGenMode.SingleModule(parseGenNavGraphsObjectConfig(true) ?: true)
    }

    private fun parseGenNavGraphsObjectConfig(isSingleModuleMode: Boolean): Boolean? {
        var generateNavGraphs = parseBoolean(GEN_NAV_GRAPHS)
        if (generateNavGraphs != null && !isSingleModuleMode) {
            logger.warn("$GEN_NAV_GRAPHS was set but mode is ${options[MODE]}, so it will be ignored. " +
                    "$GEN_NAV_GRAPHS is only meant for the default mode $MODE_SINGLE_MODULE!")
            generateNavGraphs = null
        }

        return generateNavGraphs
    }

    private fun parseBoolean(key: String): Boolean? {
        return options[key]?.runCatching {
            toBooleanStrict()
        }?.getOrElse {
            throw WrongConfigurationSetup("$key must be a boolean value!", cause = it)
        }
    }
}

class WrongConfigurationSetup(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)