@file:Suppress("SameParameterValue")

package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.ramcosta.composedestinations.codegen.commons.CORE_BOTTOM_SHEET_DESTINATION_STYLE
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
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
        private const val GEN_DEBUG_MODE = "$PREFIX.debugMode"

        private const val MERMAID_GRAPH = "$PREFIX.mermaidGraph"
        private const val HTML_MERMAID_GRAPH = "$PREFIX.htmlMermaidGraph"
    }

    fun parse(resolver: Resolver): CodeGenConfig {
        val packageName = options[GEN_PACKAGE_NAME]?.trim()?.removeSuffix(".")
        val moduleName = options[MODULE_NAME]?.trim()?.filter { it.isLetter() }
        val htmlMermaidGraph = options[HTML_MERMAID_GRAPH]?.trim()
        val mermaidGraph = options[MERMAID_GRAPH]?.trim()

        return CodeGenConfig(
            moduleName = moduleName,
            debugModeOutputDir = options[GEN_DEBUG_MODE]?.trim(),
            generateNavGraphs = parseBoolean(GEN_NAV_GRAPHS) ?: true,
            packageName = packageName,
            mermaidGraph = mermaidGraph,
            htmlMermaidGraph = htmlMermaidGraph,
            isBottomSheetDependencyPresent = resolver.isBottomSheetDepPresent()
        )
    }

    private fun parseBoolean(key: String): Boolean? {
        return options[key]?.runCatching {
            toBooleanStrict()
        }?.getOrElse {
            throw WrongConfigurationSetup("$key must be a boolean value!", cause = it)
        }
    }

    private fun Resolver.isBottomSheetDepPresent(): Boolean {
        return getClassDeclarationByName("$CORE_PACKAGE_NAME.bottomsheet.spec.$CORE_BOTTOM_SHEET_DESTINATION_STYLE") != null
    }
}

class WrongConfigurationSetup(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)