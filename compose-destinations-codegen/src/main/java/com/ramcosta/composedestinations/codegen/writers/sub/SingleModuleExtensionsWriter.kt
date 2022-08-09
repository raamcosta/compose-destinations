package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.io.OutputStream

internal class SingleModuleExtensionsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
) {

    private val importableHelper = ImportableHelper(singleModuleExtensionsTemplate.imports)

    fun write(generatedNavGraphs: List<RawNavGraphTree>) {
        val coreExtensions: OutputStream = codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = SINGLE_MODULE_EXTENSIONS
        )

        var code = singleModuleExtensionsTemplate.sourceCode

        val rootLevelNavGraphs = generatedNavGraphs.map { it.node }

        code = if (generatedNavGraphs.isNotEmpty() && rootLevelNavGraphs.size == 1) {
            val requireOptInAnnotationTypes = rootLevelNavGraphs.first().requireOptInAnnotationTypes

            val annotationsCode = StringBuilder()
            requireOptInAnnotationTypes.forEach {
                annotationsCode += "@${importableHelper.addAndGetPlaceholder(it)}\n"
            }

            code.replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, annotationsCode.toString())
                .replace(".root", ".${navGraphFieldName(rootLevelNavGraphs.first().rawNavGraphGenParams.route)}")
                .removeInstancesOf(
                    START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR,
                    END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR,
                    START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR,
                    END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR,
                    START_NAV_DESTINATION_DEPRECATED_ROOT_DEFAULT_ANCHOR,
                    END_NAV_DESTINATION_DEPRECATED_ROOT_DEFAULT_ANCHOR
                )
        } else {
            code
                .removeFromTo(START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR, END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR)
                .removeFromTo(START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR, END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR)
                .removeFromTo(START_NAV_DESTINATION_DEPRECATED_ROOT_DEFAULT_ANCHOR, END_NAV_DESTINATION_DEPRECATED_ROOT_DEFAULT_ANCHOR)
                .removeInstancesOf(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER)
        }

        coreExtensions.writeSourceFile(
            packageStatement = singleModuleExtensionsTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = code
        )
    }
}