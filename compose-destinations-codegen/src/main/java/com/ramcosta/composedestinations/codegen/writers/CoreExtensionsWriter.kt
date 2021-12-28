package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.NavGraphGeneratingParams
import com.ramcosta.composedestinations.codegen.templates.*
import java.io.OutputStream

class CoreExtensionsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
) {

    fun write(generatedNavGraphs: List<NavGraphGeneratingParams>) {
        val coreExtensions: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = CORE_EXTENSIONS
        )

        var code = coreExtensionsTemplate

        code = if (generatedNavGraphs.isNotEmpty()) {
            val requireOptInAnnotationTypes =
                generatedNavGraphs.find { it.route == "root" }!!.requireOptInAnnotationTypes

            val annotationsCode = StringBuilder()
            val additionalImports = StringBuilder()
            requireOptInAnnotationTypes.forEach {
                annotationsCode += "@${it.simpleName}\n"
                additionalImports += "import ${it.qualifiedName}\n"
            }

            code.replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, annotationsCode.toString())
                .replace(ADDITIONAL_IMPORTS, additionalImports.toString())
                .removeInstancesOf(
                    START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR,
                    END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR,
                    START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR,
                    END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR
                )
        } else {
            code
                .removeFromTo(START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR, END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR)
                .removeFromTo(START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR, END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR)
                .removeInstancesOf(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, ADDITIONAL_IMPORTS)
        }

        coreExtensions += code
        coreExtensions.close()
    }
}