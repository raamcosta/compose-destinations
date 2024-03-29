package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NAV_GRAPHS_OBJECT
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.navGraphFieldName
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
        val nonUniqueFieldNames = navGraphsParams.groupBy { navGraphFieldName(it.rawNavGraphGenParams.baseRoute) }
            .filter {
                it.value.size > 1
            }.flatMap {
                it.value
            }.map {
                it.rawNavGraphGenParams.annotationType.simpleName
            }

        if (nonUniqueFieldNames.isNotEmpty()) {
            throw IllegalDestinationsSetup(
                "NavGraphs $nonUniqueFieldNames result in the same field for the NavGraphs " +
                        "final object. Use only letters in your NavGraph annotations!"
            )
        }
    }

    private fun navGraphDeclaration(
        navGraph: RawNavGraphTree
    ): String {
        val requireOptInAnnotationsAnchor = "[REQUIRE_OPT_IN_ANNOTATIONS_ANCHOR]"

        return """
       |    ${requireOptInAnnotationsAnchor}val ${navGraphFieldName(navGraph.rawNavGraphGenParams.baseRoute)} = ${navGraph.rawNavGraphGenParams.name}
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
}
