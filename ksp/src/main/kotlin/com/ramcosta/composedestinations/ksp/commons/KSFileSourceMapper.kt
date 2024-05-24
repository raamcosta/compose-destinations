package com.ramcosta.composedestinations.ksp.commons

import com.google.devtools.ksp.symbol.KSFile

fun interface KSFileSourceMapper {

    fun mapToKSFile(sourceId: String): KSFile?
}