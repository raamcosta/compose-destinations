package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

class NavGraphsModeWriter(
    private val logger: Logger,
    private val codeGenerator: CodeOutputStreamMaker,
    private val codeGenConfig: CodeGenConfig,
) {
    private val importableHelper = ImportableHelper(moduleNavGraphTemplate.imports)

    fun write(
        navGraphs: List<RawNavGraphGenParams>,
        generatedDestinations: List<GeneratedDestination>
    ) {
        val defaultNavGraph = navGraphs.find { it.default }
        val navGraphsByType = navGraphs.associateBy { it.type }

        var destinationsByNavGraphParams = generatedDestinations.groupBy {
            if (it.navGraphInfo.isDefault) {
                defaultNavGraph ?: rootNavGraphGenParams
            } else {
                val info = it.navGraphInfo as NavGraphInfo.AnnotatedSource
                navGraphsByType[info.graphType] ?: rootNavGraphGenParams
            }
        }

        if (destinationsByNavGraphParams.containsKey(rootNavGraphGenParams)) {
            val moduleName = codeGenConfig.moduleName ?: throw IllegalDestinationsSetup(
                "You need to set 'moduleName' on gradle ksp configuration to be used as" +
                        " main nav graph name or use a NavGraph annotation on all Destinations of this module."
            )

            destinationsByNavGraphParams = destinationsByNavGraphParams.toMutableMap()
                .apply {
                    val newRootWithModuleName =
                        rootNavGraphGenParams.copyWithNameForRoute(newRoute = moduleName)

                    val isThereAlreadyOneWithTheNameOrRoute =
                        keys.any { it.route == newRootWithModuleName.route || it.name == newRootWithModuleName.name }

                    if (isThereAlreadyOneWithTheNameOrRoute) {
                        throw IllegalDestinationsSetup(
                            "Code gen mode was set to 'navgraphs' but you're using both `RootNavGraph` " +
                                    "and a NavGraph with the module name. On this mode, `RootNavGraph` is replaced " +
                                    "by one with the name's module, so these collide!\n" +
                                    "FIX: Create your own NavGraph annotation with `NavGraph(default = true)` to be used instead of `RootNavGraph`"
                        )
                    }

                    put(newRootWithModuleName, remove(rootNavGraphGenParams)!!)
                }
        }

        val rawNavGraphGenByParent: Map<Importable?, List<RawNavGraphGenParams>> =
            destinationsByNavGraphParams.keys.groupBy { it.parent }

        destinationsByNavGraphParams.forEach {
            val nestedNavGraphs = rawNavGraphGenByParent[it.key.type] ?: emptyList()

            writeNavGraph(it.key.name, it.key.route, nestedNavGraphs, it.value)
        }
    }

    private fun writeNavGraph(
        navGraphName: String,
        navGraphRoute: String,
        nestedNavGraphs: List<RawNavGraphGenParams>,
        destinations: List<GeneratedDestination>
    ) {
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
                    .replace(NAV_GRAPH_ROUTE_PLACEHOLDER, "\"$navGraphRoute\"")
                    .replace(
                        NAV_GRAPH_START_ROUTE_PLACEHOLDER,
                        startingDestination(codeGenConfig, navGraphName, destinations, nestedNavGraphs)
                    )
                    .replace(NAV_GRAPH_DESTINATIONS, navGraphDestinationsCode(destinations))
                    .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, requireOptInAnnotations(destinations))
                    .replace(NESTED_NAV_GRAPHS, nestedNavGraphsCode(nestedNavGraphs))
            )
    }

    private fun nestedNavGraphsCode(nestedNavGraphs: List<RawNavGraphGenParams>): String {
        if (nestedNavGraphs.isEmpty()) {
            return ""
        }

        return """

            override val nestedNavGraphs = listOf(
                %s1
            )
            
        """.trimIndent()
            .prependIndent("\t")
            .replace("%s1", nestedNavGraphs.joinToString(", \n\t\t") { it.type.simpleName })
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
        val requireOptInClassTypes =
            generatedDestinations.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            code += "@${importableHelper.addAndGetPlaceholder(annotationType)}\n"
        }

        return code.toString()
    }
}
