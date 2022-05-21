package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.sanitizePackageName
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.templates.ADDITIONAL_IMPORTS
import com.ramcosta.composedestinations.codegen.templates.NAV_ARGS_METHOD_WHEN_CASES
import com.ramcosta.composedestinations.codegen.templates.REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navArgsGettersTemplate
import java.io.OutputStream

class NavArgsGettersWriter(
    private val codeGenerator: CodeOutputStreamMaker
) {

    private val additionalImports = mutableSetOf<String>()

    fun write(generatedDestinations: List<GeneratedDestination>) {
        if (generatedDestinations.all { it.navArgsImportable == null }) {
            return
        }

        val file: OutputStream = codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = "NavArgsGetters",
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        )

        val destinationsWithNavArgs = generatedDestinations.filter { it.navArgsImportable != null }
            .associateBy { it.navArgsImportable!! }
            .values
            .toList()

        file += navArgsGettersTemplate
            .replace(NAV_ARGS_METHOD_WHEN_CASES, navArgsMethodWhenCases(destinationsWithNavArgs))
            .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, requireOptInAnnotations(destinationsWithNavArgs))
            .replace(ADDITIONAL_IMPORTS, additionalImports())
        file.close()
    }

    private fun requireOptInAnnotations(destinationsWithNavArgs: List<GeneratedDestination>): String {
        val requireOptInClassTypes = destinationsWithNavArgs.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            additionalImports.add(annotationType.qualifiedName)
            code += "@${annotationType.simpleName}\n"
        }

        return code.toString()
    }

    private fun navArgsMethodWhenCases(destinationsWithNavArgs: List<GeneratedDestination>): String {
        val sb = StringBuilder()

        destinationsWithNavArgs.forEachIndexed { idx, it ->
            sb += "\t\t${it.navArgsImportable!!.simpleName}::class.java -> ${it.simpleName}.argsFrom(savedStateHandle) as T"

            additionalImports.add(it.qualifiedName)
            additionalImports.add(it.navArgsImportable!!.qualifiedName)

            if (idx < destinationsWithNavArgs.lastIndex) {
                sb += "\n"
            }
        }

        return sb.toString()
    }

    private fun additionalImports(): String {
        val sb = StringBuilder()

        additionalImports.sorted().forEachIndexed { idx, it ->
            sb += "import ${it.sanitizePackageName()}"

            if (idx < additionalImports.size - 1) {
                sb += "\n"
            }
        }

        return sb.toString()
    }


}
