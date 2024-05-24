package com.ramcosta.composedestinations.ksp.commons

import com.google.devtools.ksp.symbol.KSFile

internal class MutableKSFileSourceMapper: KSFileSourceMapper {

    private val sourceFilesById = mutableMapOf<String, KSFile?>()

    operator fun set(sourceId: String, file: KSFile?) {
        sourceFilesById[sourceId] = file
    }

    override fun mapToKSFile(sourceId: String): KSFile? {
        return sourceFilesById[sourceId]
    }

}