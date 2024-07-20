package com.ramcosta.composedestinations.codegen.facades

import java.io.File

interface Logger {
    val debugMode: Boolean

    val debugModeOutputPath: String?

    val prettyPrinter: PPrinter

    fun logging(message: String)

    fun info(message: String)

    fun warn(message: String)

    fun error(message: String)

    fun exception(e: Throwable)

    companion object {
        lateinit var instance: Logger
    }
}

interface PPrinter {
    fun pprint(any: Any): String
}

inline fun Logger.debug(message: PPrinter.() -> String) = synchronized(this) {
    debugModeOutputPath?.let {
        File(it)
            .run {
                parentFile.mkdirs()
                appendText("***************************************\n")
                appendText(message(prettyPrinter))
                appendText("\n***************************************\n")
                appendText("\n\n")
            }
    }
}