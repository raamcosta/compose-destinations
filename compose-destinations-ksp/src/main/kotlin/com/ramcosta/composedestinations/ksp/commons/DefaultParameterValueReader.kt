package com.ramcosta.composedestinations.ksp.commons

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSValueParameter
import java.io.File

object DefaultParameterValueReader {

    fun readDefaultValue(
        lineText: String,
        argName: String,
        argType: String,
    ): String {
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

        return if (auxText.startsWith("\"")) {
            stringValue(auxText)
        } else {
            nonStringValue(auxText)
        }
    }

    private fun stringValue(auxText: String): String {
        var finalText = auxText
        val splits = finalText.split("\"")
        finalText = splits[1]

        var i = 2
        while (finalText.endsWith('\\')) {
            finalText += "\"${splits[i]}"
            i++
        }

        return "\"$finalText\""
    }

    private fun nonStringValue(auxText: String): String {
        var auxText1 = auxText
        val index = auxText1.indexOfFirst { it == ' ' || it == ',' || it == '\n' || it == ')' }

        if (index != -1)
            auxText1 = auxText1.removeRange(index, auxText1.length)

        return auxText1
    }
}

fun KSValueParameter.getDefaultValue(): String? {
    if (!hasDefault) return null

    /*
        This is not ideal: having to read the first n lines of the file,
        and parse the default value manually from the source code
        I haven't found a better way yet, seems like there is no other
        way in KSP :/
    */

    val fileLocation = location as FileLocation
    val lineText = File(fileLocation.filePath).readLine(fileLocation.lineNumber)

    return DefaultParameterValueReader.readDefaultValue(lineText, name!!.asString(), type.toString())
}
