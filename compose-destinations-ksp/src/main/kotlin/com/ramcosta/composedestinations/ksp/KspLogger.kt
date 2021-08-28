package com.ramcosta.composedestinations.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.ramcosta.composedestinations.codegen.facades.Logger

class KspLogger(
    private val kspLogger: KSPLogger
): Logger {

    override fun logging(message: String) = kspLogger.logging(message)

    override fun info(message: String) = kspLogger.info(message)

    override fun warn(message: String) = kspLogger.warn(message)

    override fun error(message: String) = kspLogger.error(message)

    override fun exception(e: Throwable) = kspLogger.exception(e)
}