package com.ramcosta.composedestinations.ksp.codegen

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.ksp.commons.KSFileSourceMapper
import java.io.OutputStream

class KspCodeOutputStreamMaker(
    private val codeGenerator: CodeGenerator,
    private val sourceMapper: KSFileSourceMapper
) : CodeOutputStreamMaker {

    override fun makeFile(
        name: String,
        packageName: String,
        extensionName: String,
        vararg sourceIds: String
    ): OutputStream {

        val sources = sourceIds.mapNotNull { sourceMapper.mapToKSFile(it) }.toTypedArray()
        val dependencies = if (sources.isEmpty()) {
            Dependencies.ALL_FILES
        } else {
            Dependencies(
                true,
                *sources
            )
        }

        return codeGenerator.createNewFile(
            dependencies = dependencies,
            fileName = name,
            packageName = packageName,
            extensionName = extensionName
        )
    }
}