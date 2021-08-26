package com.ramcosta.composedestinations.utils

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSValueParameter
import com.ramcosta.composedestinations.model.DefaultValue
import java.io.File

internal class DefaultParameterValueReader {

    internal fun readDefaultValue(
        lineText: String,
        argName: String,
        argType: String,
    ): DefaultValue {
        var auxText = lineText

        val anchors = arrayOf(
            argName,
            ":",
            argType,
            "="
        )

        var index: Int
        anchors.forEach {
            index = auxText.indexOf(it)
            auxText = auxText.removeRange(0, index)
        }
        auxText = auxText.removeRange(0, 1)

        index = auxText.indexOfFirst { it != ' ' }
        auxText = auxText.removeRange(0, index)

        index = auxText.indexOfFirst { it == ' ' || it == ',' || it == '\n' || it == ')' }
        if (index != -1)
            auxText = auxText.removeRange(index, auxText.length)

        return DefaultValue.Known(auxText)
    }
}

internal val reader = DefaultParameterValueReader()

internal fun KSValueParameter.getDefaultValue(): DefaultValue {
    if (!hasDefault) return DefaultValue.None

    /*
        This is not ideal: having to read the first n lines of the file,
        and parse the default value manually from the source code
        I haven't found a better way yet, seems like there is no other
        way in KSP :/
    */

    val fileLocation = location as FileLocation
    val lineText = File(fileLocation.filePath).readLine(fileLocation.lineNumber)

    return reader.readDefaultValue(lineText, name!!.asString(), type.toString())
}
