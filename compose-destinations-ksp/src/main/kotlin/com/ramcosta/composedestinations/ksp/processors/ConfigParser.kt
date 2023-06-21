@file:Suppress("SameParameterValue")

package com.ramcosta.composedestinations.ksp.processors

import com.ramcosta.composedestinations.codegen.model.CodeGenConfig

class ConfigParser(
    private val options: Map<String, String>
) {

    companion object {
        private const val PREFIX = "compose-destinations"

        // Configs
        private const val GEN_NAV_GRAPHS = "$PREFIX.generateNavGraphs"
        private const val GEN_PACKAGE_NAME = "$PREFIX.codeGenPackageName"
        private const val MODULE_NAME = "$PREFIX.moduleName"
    }

    fun parse(): CodeGenConfig {
        val packageName = options[GEN_PACKAGE_NAME]?.trim()?.removeSuffix(".")
        val moduleName = options[MODULE_NAME]?.trim()?.filter { it.isLetter() }

        return CodeGenConfig(
            moduleName = moduleName,
            generateNavGraphs = parseBoolean(GEN_NAV_GRAPHS) ?: true,
            packageName = packageName,
        )
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