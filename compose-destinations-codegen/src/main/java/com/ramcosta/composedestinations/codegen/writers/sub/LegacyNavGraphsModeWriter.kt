package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.NavGraphInfo
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.util.*

class LegacyNavGraphsModeWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val codeGenConfig: CodeGenConfig
) {

    private val importableHelper = ImportableHelper(moduleNavGraphTemplate.imports)

    fun write(generatedDestinations: List<GeneratedDestination>) {
        val navGraphNames = generatedDestinations.map { it.asLegacy().navGraphRoute }
        val moduleName = codeGenConfig.moduleName
        if (navGraphNames.size > 1
            && navGraphNames.contains("root")
            && (moduleName == null || navGraphNames.contains(moduleName))
        ) {
            throw IllegalDestinationsSetup(
                "Code gen mode was set to 'navgraphs' but you're using multiple" +
                        "navGraphs and one is the default 'root'. \nFIX: Set nav graphs on all @Destination " +
                        "or set a module name via config to be used as the default nav graph name instead of 'root'"
            )
        }

        generatedDestinations.map {
            val navGraphInfo = it.asLegacy()
            if (navGraphInfo.navGraphRoute == "root") {
                it.copy(
                    navGraphInfo = navGraphInfo.copy(
                        navGraphRoute = codeGenConfig.moduleName
                            ?: throw IllegalDestinationsSetup(
                                "You need to set 'moduleName' on gradle ksp configuration to be used as" +
                                        " main nav graph name or set 'navGraph' on all @Destination of this module."
                            )
                    )
                )
            } else {
                it
            }
        }.groupBy {
            it.asLegacy().navGraphRoute
        }.forEach {
            writeNavGraph(it.key, it.value)
        }
    }

    private fun GeneratedDestination.asLegacy() =
        (navGraphInfo as NavGraphInfo.Legacy)

    private fun writeNavGraph(navGraphRoute: String, destinations: List<GeneratedDestination>) {
        val navGraphName = navGraphRoute.replaceFirstChar { it.uppercase(Locale.US) } + "NavGraph"
        codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = navGraphName,
            sourceIds = sourceIds(destinations).toTypedArray()
        )
            .writeSourceFile(
                packageStatement = moduleNavGraphTemplate.packageStatement,
                importableHelper = importableHelper,
                sourceCode = moduleNavGraphTemplate.sourceCode
                    .replace(NAV_GRAPH_NAME_PLACEHOLDER, navGraphName)
                    .replace(NAV_GRAPH_ROUTE_PLACEHOLDER, "\"${navGraphRoute.toSnakeCase()}\"")
                    .replace(NAV_GRAPH_START_ROUTE_PLACEHOLDER, legacyStartingDestination(navGraphRoute, destinations).simpleName)
                    .replace(NAV_GRAPH_DESTINATIONS, navGraphDestinationsCode(destinations))
                    .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, requireOptInAnnotations(destinations))
                    .removeInstancesOf(NESTED_NAV_GRAPHS)
            )
    }

    private fun navGraphDestinationsCode(destinations: List<GeneratedDestination>): String {
        val code = StringBuilder()
        destinations.forEachIndexed { idx, it ->
            code += "\t\t${it.simpleName}"

            if (idx != destinations.lastIndex) {
                code += ",\n"
            }
        }

        return code.toString()
    }

    private fun requireOptInAnnotations(generatedDestinations: List<GeneratedDestination>): String {
        val requireOptInClassTypes = generatedDestinations.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            code += "@${importableHelper.addAndGetPlaceholder(annotationType)}\n"
        }

        return code.toString()
    }
}