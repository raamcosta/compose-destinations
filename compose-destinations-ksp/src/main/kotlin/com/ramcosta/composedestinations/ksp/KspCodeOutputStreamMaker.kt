package com.ramcosta.composedestinations.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import java.io.OutputStream

class KspCodeOutputStreamMaker(
    private val codeGenerator: CodeGenerator
) : CodeOutputStreamMaker {

    override fun makeFile(name: String, packageName: String): OutputStream {
        return codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            fileName = name,
            packageName = packageName
        )
    }
}