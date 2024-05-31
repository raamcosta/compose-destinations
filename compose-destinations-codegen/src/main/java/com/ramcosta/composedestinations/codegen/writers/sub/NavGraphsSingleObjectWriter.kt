package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NAV_GRAPHS_OBJECT
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.CustomNavType
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.moduleName
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_PRETTY_KDOC_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.templates.navGraphsObjectTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.NavArgResolver
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

internal class NavGraphsSingleObjectWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val customNavTypeByType: Map<Type, CustomNavType>,
    private val singleNavGraphWriter: (
        CodeOutputStreamMaker,
        ImportableHelper,
        RawNavGraphTree,
        NavArgResolver
    ) -> SingleNavGraphWriter,
) {

    private val importableHelper = ImportableHelper(navGraphsObjectTemplate.imports)

    private fun navGraphWriter(rawNavGraphTree: RawNavGraphTree) = singleNavGraphWriter(
        codeGenerator,
        ImportableHelper(navGraphsObjectTemplate.imports),
        rawNavGraphTree,
        NavArgResolver(customNavTypeByType, importableHelper)
    )

    fun write(
        graphTrees: List<RawNavGraphTree>,
        generatedDestinations: List<CodeGenProcessedDestination>
    ) {
        val flattenGraphs = graphTrees.flatten()
        checkUniquenessOnNavGraphFieldNames(flattenGraphs)

        graphTrees.forEach { writeNavGraphTreeRecursively(it) }

        writeFile(generatedDestinations, graphTrees, flattenGraphs)
    }

    private fun writeNavGraphTreeRecursively(
        graphTree: RawNavGraphTree
    ) {
        graphTree.nestedGraphs.forEach {
            writeNavGraphTreeRecursively(it)
        }
        navGraphWriter(graphTree).write()
    }

    private fun writeFile(
        generatedDestinations: List<CodeGenProcessedDestination>,
        topLevelGraphs: List<RawNavGraphTree>,
        flattenGraphs: List<RawNavGraphTree>
    ) {
        codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = "$moduleName$GENERATED_NAV_GRAPHS_OBJECT",
            sourceIds = sourceIds(generatedDestinations, flattenGraphs).toTypedArray()
        ).writeSourceFile(
            packageStatement = navGraphsObjectTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = navGraphsObjectTemplate.sourceCode
                .replace(NAV_GRAPHS_PRETTY_KDOC_PLACEHOLDER, NavGraphsPrettyKdocWriter(importableHelper, topLevelGraphs).write())
                .replace(NAV_GRAPHS_PLACEHOLDER, navGraphsDeclaration(flattenGraphs))
                .let {
                    if (generatedDestinations.isEmpty()) {
                        importableHelper.remove(setOfImportable("${codeGenBasePackageName}.destinations.*"))
                    }
                    it
                }
        )
    }

    private fun List<RawNavGraphTree>.flatten(): List<RawNavGraphTree> {
        return this + flatMap { it.nestedGraphs.flatten() }
    }

    private fun navGraphsDeclaration(navGraphsParams: List<RawNavGraphTree>): String {
        val navGraphsDeclaration = StringBuilder()

        navGraphsParams.forEachIndexed { idx, navGraphParams ->
            navGraphsDeclaration += navGraphDeclaration(navGraphParams)

            if (idx != navGraphsParams.lastIndex) {
                navGraphsDeclaration += "\n\n"
            }
        }

        return navGraphsDeclaration.toString()
    }

    private fun checkUniquenessOnNavGraphFieldNames(navGraphsParams: List<RawNavGraphTree>) {
        val nonUniqueRoutesToAnnotationName: List<Pair<String, String>> = navGraphsParams.groupBy { it.navGraphFieldName() }
            .filter {
                it.value.size > 1
            }.flatMap { mapEntry ->
                mapEntry.value.map { mapEntry.key to it }
            }.map { (route, graph) ->
                route to graph.rawNavGraphGenParams.annotationType.simpleName
            }

        if (nonUniqueRoutesToAnnotationName.isNotEmpty()) {
            if (nonUniqueRoutesToAnnotationName.any { it.first == "main" }) {
                throw IllegalDestinationsSetup(
                    "NavGraphs ${nonUniqueRoutesToAnnotationName.map { it.second }} result in the same field for the NavGraphs " +
                    "final object. You cannot have a Graph annotation called `${nonUniqueRoutesToAnnotationName.map { it.second }.firstOrNull { it.startsWith("main", ignoreCase = true) } ?: "MainGraph"}` and also have one with the same name as the module name."
                )
            }

            throw IllegalDestinationsSetup(
                "NavGraphs ${nonUniqueRoutesToAnnotationName.map { it.second }} result in the same field for the NavGraphs " +
                        "final object. Use only letters in your NavGraph annotations!"
            )
        }
    }

    private fun navGraphDeclaration(
        navGraph: RawNavGraphTree
    ): String {
        val requireOptInAnnotationsAnchor = "[REQUIRE_OPT_IN_ANNOTATIONS_ANCHOR]"

        return """
       |    ${requireOptInAnnotationsAnchor}val ${navGraph.navGraphFieldName()} = ${navGraph.rawNavGraphGenParams.name}
        """.trimMargin()
            .replace(
                requireOptInAnnotationsAnchor,
                requireOptInAnnotations(navGraph.requireOptInAnnotationTypes)
            )

    }

    private fun requireOptInAnnotations(navGraphRequireOptInImportables: Set<Importable>): String {
        val code = StringBuilder()

        navGraphRequireOptInImportables.forEach { annotationType ->
            code += "@${importableHelper.addAndGetPlaceholder(annotationType)}\n\t"
        }

        return code.toString()
    }

    private fun RawNavGraphTree.navGraphFieldName(): String {
        val navGraphRoute = rawNavGraphGenParams.baseRouteWithNoModulePrefix
        val regex = "[^a-zA-Z]".toRegex()
        val auxNavGraphRoute = navGraphRoute.toCharArray().toMutableList()
        var weirdCharIndex = auxNavGraphRoute.indexOfFirst { it.toString().matches(regex) }

        while (weirdCharIndex != -1) {
            auxNavGraphRoute.removeAt(weirdCharIndex)
            if (weirdCharIndex >= auxNavGraphRoute.size) {
                break
            }
            auxNavGraphRoute[weirdCharIndex] = auxNavGraphRoute[weirdCharIndex].uppercaseChar()

            weirdCharIndex = auxNavGraphRoute.indexOfFirst { it.toString().matches(regex) }
        }

        val fieldName = String(auxNavGraphRoute.toCharArray())
        return if (fieldName.equals(moduleName, ignoreCase = true)) {
            // to avoid things like "LoginNavGraphs.login" and have instead "LoginNavGraphs.main"
            "main"
        } else {
            fieldName
        }
    }
}
