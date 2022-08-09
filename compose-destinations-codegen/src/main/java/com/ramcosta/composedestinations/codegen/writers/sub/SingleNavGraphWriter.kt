package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.commons.startingDestinationName
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.writers.destinationsPackageName
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

val navGraphsPackageName = "$codeGenBasePackageName.navgraphs"

internal class SingleNavGraphWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val config: Config,
    private val importableHelper: ImportableHelper
) {

    fun write(navGraph: RawNavGraphTree) {
        val file = moduleNavGraphTemplate.sourceCode
            .replace(NAV_GRAPH_NAME_PLACEHOLDER, navGraph.node.rawNavGraphGenParams.name)
            .replace(NAV_GRAPH_ROUTE_PLACEHOLDER, "\"${navGraph.node.rawNavGraphGenParams.route}\"")
            .replace(
                NAV_GRAPH_TYPE,
                if (navGraph.node.navArgs != null) {
                    val preferredName =
                        if (navGraph.node.navArgs.qualifiedName.startsWith(destinationsPackageName)) {
                            navGraph.node.navArgs.qualifiedName.removePrefix("$destinationsPackageName.")
                        } else {
                            importableHelper.addAndGetPlaceholder(navGraph.node.navArgs)
                        }
                    "${importableHelper.addAndGetPlaceholder(config.navGraphType)}<$preferredName>${if (!config.navTypeInterface) "()" else ""}"
                } else {
                    "${importableHelper.addAndGetPlaceholder(config.directionNavGraphType)}${if (!config.directionTypeInterface) "()" else ""}"
                }
            )
            .replace(
                NAV_GRAPH_START_ROUTE_PLACEHOLDER,
                startingDestinationName(navGraph)
            )
            .replace(
                NAV_GRAPH_DESTINATIONS,
                navGraphDestinationsCode(navGraph.node.destinations)
            )
            .replace(
                REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER,
                importableHelper.requireOptInAnnotations(navGraph.node.destinations)
            )
            .replace(
                NESTED_NAV_GRAPHS,
                nestedNavGraphsCode(navGraph.nestedGraphs.map { it.node.rawNavGraphGenParams })
            )
            .replace(NAV_GRAPH_DESTINATIONS_FIELD_NAME, config.destinationsCollectionName)
            .replace(
                NAV_GRAPH_DESTINATIONS_FIELD_ASSOCIATE_BY,
                if (config.destinationsCollectionIsMap) {
                    ".associateBy { it.route }"
                } else {
                    ""
                }
            )

        codeGenerator.makeFile(
            packageName = navGraphsPackageName,
            name = navGraph.node.rawNavGraphGenParams.name,
            sourceIds = sourceIds(navGraph.node.destinations).toTypedArray()
        )
            .writeSourceFile(
                packageStatement = moduleNavGraphTemplate.packageStatement,
                importableHelper = importableHelper,
                sourceCode = file
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


    private fun ImportableHelper.requireOptInAnnotations(generatedDestinations: List<GeneratedDestination>): String {
        val requireOptInClassTypes =
            generatedDestinations.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            code += "@${addAndGetPlaceholder(annotationType)}\n"
        }

        return code.toString()
    }

    private fun nestedNavGraphsCode(nestedNavGraphs: List<RawNavGraphGenParams>): String {
        if (nestedNavGraphs.isEmpty()) {
            return ""
        }

        return """

            override val nestedNavGraphs get() = listOf(
                %s1
            )
            
        """.trimIndent()
            .prependIndent("\t")
            .replace("%s1", nestedNavGraphs.joinToString(", \n\t\t") { it.type.simpleName })
    }


    class Config(
        val navGraphType: Importable,
        val navTypeInterface: Boolean,
        val directionNavGraphType: Importable,
        val directionTypeInterface: Boolean,
        val destinationsCollectionName: String,
        val destinationsCollectionIsMap: Boolean
    )
}

