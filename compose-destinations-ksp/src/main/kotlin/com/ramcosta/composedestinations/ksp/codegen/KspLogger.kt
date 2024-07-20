package com.ramcosta.composedestinations.ksp.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.facades.PPrinter
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import io.exoquery.fansi.Attrs
import java.util.Locale

class KspLogger(
    private val codeGenConfig: CodeGenConfig,
    private val kspLogger: KSPLogger
): Logger {

    override val debugMode: Boolean = codeGenConfig.debugModeOutputDir != null
    override val debugModeOutputPath: String? = codeGenConfig.debugModeOutputDir?.let {
        "$it/composeDestinationsDebug/${codeGenConfig.registryId.lowercase(Locale.ROOT)}.txt"
    }

    private val _prettyPrinter: PPrinter = object: PPrinter {
        override fun pprint(any: Any): String {
            return io.exoquery.pprint(any, defaultHeight = Int.MAX_VALUE, colorLiteral = Attrs.Empty, colorApplyPrefix = Attrs.Empty).toString()
        }
    }
    override val prettyPrinter: PPrinter
        get() {
            check(debugMode)
            return _prettyPrinter
        }

    override fun logging(message: String) = kspLogger.logging(message)

    override fun info(message: String) = kspLogger.info(message)

    override fun warn(message: String) = kspLogger.warn(message)

    override fun error(message: String) = kspLogger.error(message)

    override fun exception(e: Throwable) = kspLogger.exception(e)
}
