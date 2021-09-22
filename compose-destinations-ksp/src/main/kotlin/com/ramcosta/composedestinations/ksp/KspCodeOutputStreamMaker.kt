package com.ramcosta.composedestinations.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import java.io.OutputStream

internal class KspCodeOutputStreamMaker(
    private val codeGenerator: CodeGenerator,
    private val sourceMapper: KSFileSourceMapper,
) : CodeOutputStreamMaker {

    override fun makeFile(name: String,
                          packageName: String,
                          vararg sourceIds: String
    ): OutputStream {

        return codeGenerator.createNewFile(
            dependencies = Dependencies(
                true,
                *sourceIds.mapNotNull { sourceMapper.mapToKSFile(it) }.toTypedArray()
            ),
            fileName = name,
            packageName = packageName
        )
    }

    fun interface KSFileSourceMapper {

        fun mapToKSFile(sourceId: String): KSFile?
    }
}