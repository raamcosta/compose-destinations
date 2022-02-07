package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.templates.*
import java.io.OutputStream
import java.util.*

class NavGraphsModeWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val codeGenConfig: CodeGenConfig
) {

    private val additionalImports = mutableSetOf<String>()

    fun write(generatedDestinations: List<GeneratedDestination>) {
        val navGraphs = generatedDestinations.mapTo(mutableSetOf()) { it.navGraphRoute }

        val moduleName = codeGenConfig.moduleName
        if (navGraphs.size > 1
            && navGraphs.contains("root")
            && (moduleName == null || navGraphs.contains(moduleName))
        ) {
            throw IllegalDestinationsSetup(
                "Code gen mode was set to 'navgraphs' but you're using multiple" +
                        "navGraphs and one is the default 'root'. \nFIX: Set nav graphs on all @Destination " +
                        "or set a module name via config to be used as the default nav graph name instead of 'root'"
            )
        }

        generatedDestinations.map {
            if (it.navGraphRoute == "root") {
                it.copy(
                    navGraphRoute = moduleName
                        ?: throw IllegalDestinationsSetup(
                            "You need to set 'moduleName' on gradle ksp configuration to be used as" +
                                    " main nav graph name or set 'navGraph' on all @Destination of this module."
                        )
                )
            } else {
                it
            }
        }.groupBy {
            it.navGraphRoute
        }.forEach {
            writeNavGraph(it.key, it.value)
        }
    }

    private fun writeNavGraph(navGraphRoute: String, destinations: List<GeneratedDestination>) {
        val navGraphName = navGraphRoute.replaceFirstChar { it.uppercase(Locale.US) } + "NavGraph"
        val file: OutputStream = codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = navGraphName,
            sourceIds = sourceIds(destinations).toTypedArray()
        )

        file += moduleNavGraphTemplate
            .replace(NAV_GRAPH_NAME_PLACEHOLDER, navGraphName)
            .replace(NAV_GRAPH_ROUTE_PLACEHOLDER, "\"${navGraphRoute.toSnakeCase()}\"")
            .replace(NAV_GRAPH_START_ROUTE_PLACEHOLDER, startingDestination(navGraphRoute, destinations))
            .replace(NAV_GRAPH_DESTINATIONS, navGraphDestinationsCode(destinations))
            .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, requireOptInAnnotations(destinations))
            .replace(ADDITIONAL_IMPORTS, additionalImports())

        file.close()
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

    private fun additionalImports(): String {
        val imports = StringBuilder()

        additionalImports.sorted().forEachIndexed { idx, it ->
            if (idx == 0) imports += "\n"

            imports += "import $it\n"
        }

        return imports.toString()
    }

    private fun requireOptInAnnotations(generatedDestinations: List<GeneratedDestination>): String {
        val requireOptInClassTypes = generatedDestinations.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            additionalImports.add(annotationType.qualifiedName)
            code += "@${annotationType.simpleName}\n"
        }

        return code.toString()
    }
}