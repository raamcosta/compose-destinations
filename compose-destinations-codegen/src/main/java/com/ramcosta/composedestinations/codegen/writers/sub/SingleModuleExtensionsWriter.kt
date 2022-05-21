package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.NavGraphGeneratingParams
import com.ramcosta.composedestinations.codegen.templates.*
import java.io.OutputStream

class SingleModuleExtensionsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger
) {

    fun write(generatedNavGraphs: List<NavGraphGeneratingParams>) {
        val coreExtensions: OutputStream = codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = SINGLE_MODULE_EXTENSIONS
        )

        var code = singleModuleExtensionsTemplate

        val nestedNavGraphs: List<NavGraphGeneratingParams> = generatedNavGraphs
            .flatMapTo(mutableSetOf()) { it.nestedNavGraphRoutes }
            .map { route ->
                generatedNavGraphs.find { it.route == route }
                    ?: throw UnexpectedException("Check your NavGraphs annotations and their imports!")
            }

        val rootLevelNavGraphs = generatedNavGraphs - nestedNavGraphs

        code = if (generatedNavGraphs.isNotEmpty() && rootLevelNavGraphs.size == 1) {
            val requireOptInAnnotationTypes = rootLevelNavGraphs.first().requireOptInAnnotationTypes

            val annotationsCode = StringBuilder()
            val additionalImports = StringBuilder()
            requireOptInAnnotationTypes.forEach {
                annotationsCode += "@${it.simpleName}\n"
                additionalImports += "\nimport ${it.qualifiedName.sanitizePackageName()}"
            }

            code.replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, annotationsCode.toString())
                .replace(ADDITIONAL_IMPORTS, additionalImports.toString())
                .replace(".root", ".${navGraphFieldName(rootLevelNavGraphs.first().route)}")
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
                .removeInstancesOf(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, ADDITIONAL_IMPORTS)
        }

        coreExtensions += code
        coreExtensions.close()
    }
}