package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navGraphsObjectTemplate
import com.ramcosta.composedestinations.codegen.writers.SealedNavGraphWriter
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

internal class NavGraphsSingleObjectWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val sealedNavGraphWriter: SealedNavGraphWriter,
    private val singleNavGraphWriter: (
        CodeOutputStreamMaker,
        SingleNavGraphWriter.Config,
        ImportableHelper
    ) -> SingleNavGraphWriter,
) {

    private val importableHelper = ImportableHelper(navGraphsObjectTemplate.imports)

    private val navGraphWriter = singleNavGraphWriter(
        codeGenerator,
        SingleNavGraphWriter.Config(
            navGraphType = Importable(GENERATED_TYPED_NAV_GRAPH, "$navGraphsPackageName.$GENERATED_TYPED_NAV_GRAPH"),
            navTypeInterface = false,
            directionNavGraphType = Importable(GENERATED_DIRECTION_NAV_GRAPH, "$navGraphsPackageName.$GENERATED_DIRECTION_NAV_GRAPH"),
            directionTypeInterface = false,
            destinationsCollectionIsMap = false,
            destinationsCollectionName = "destinations"
        ),
        importableHelper
    )

    fun write(
        graphTrees: List<RawNavGraphTree>,
        generatedDestinations: List<GeneratedDestination>
    ) {
        val flattenGraphs = graphTrees.flatten()
        checkUniquenessOnNavGraphFieldNames(flattenGraphs)

        graphTrees.forEach { writeNavGraphTreeRecursively(it) }

        writeFile(generatedDestinations, flattenGraphs)
        sealedNavGraphWriter.write()
    }

    private fun writeNavGraphTreeRecursively(
        graphTree: RawNavGraphTree
    ) {
        graphTree.nestedGraphs.forEach {
            writeNavGraphTreeRecursively(it)
        }
        navGraphWriter.write(graphTree)
    }

    private fun writeFile(
        generatedDestinations: List<GeneratedDestination>,
        navGraphsParams: List<RawNavGraphNode>
    ) {
        codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = GENERATED_NAV_GRAPHS_OBJECT,
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        ).writeSourceFile(
            packageStatement = navGraphsObjectTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = navGraphsObjectTemplate.sourceCode
                .replace(NAV_GRAPHS_PLACEHOLDER, navGraphsDeclaration(navGraphsParams))
        )
    }

    private fun List<RawNavGraphTree>.flatten(): List<RawNavGraphNode> {
        return this.map { it.node } + flatMap { it.nestedGraphs.flatten() }
    }

    private fun navGraphsDeclaration(navGraphsParams: List<RawNavGraphNode>): String {
        val navGraphsDeclaration = StringBuilder()

        navGraphsParams.forEachIndexed { idx, navGraphParams ->
            navGraphsDeclaration += navGraphDeclaration(navGraphParams)

            if (idx != navGraphsParams.lastIndex) {
                navGraphsDeclaration += "\n\n"
            }
        }

        return navGraphsDeclaration.toString()
    }

    private fun checkUniquenessOnNavGraphFieldNames(navGraphsParams: List<RawNavGraphNode>) {
        val nonUniqueFieldNames = navGraphsParams.groupBy { navGraphFieldName(it.rawNavGraphGenParams.route) }
            .filter {
                it.value.size > 1
            }.flatMap {
                it.value
            }.map {
                it.rawNavGraphGenParams.type.simpleName
            }

        if (nonUniqueFieldNames.isNotEmpty()) {
            throw IllegalDestinationsSetup(
                "NavGraphs $nonUniqueFieldNames result in the same field for the NavGraphs " +
                        "final object. Use only letters in your NavGraph annotations!"
            )
        }
    }

    private fun navGraphDeclaration(
        navGraph: RawNavGraphNode
    ): String {
        val requireOptInAnnotationsAnchor = "[REQUIRE_OPT_IN_ANNOTATIONS_ANCHOR]"

        return """
       |    ${requireOptInAnnotationsAnchor}val ${navGraphFieldName(navGraph.rawNavGraphGenParams.route)} = ${navGraph.rawNavGraphGenParams.name}
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
