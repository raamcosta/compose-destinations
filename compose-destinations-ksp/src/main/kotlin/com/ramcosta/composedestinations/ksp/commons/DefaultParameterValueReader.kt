package com.ramcosta.composedestinations.ksp.commons

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.NonExistLocation
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.removeFromTo
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.DefaultValue
import java.io.File

object DefaultParameterValueReader {

    fun readDefaultValue(
        resolver: (pckg: String, name: String) -> ResolvedSymbol?,
        srcCodeLines: List<String>,
        packageName: String,
        imports: List<String>,
        argName: String,
        argType: String,
    ): DefaultValue {
        var auxText = srcCodeLines.joinToString("") { it.trim() }

        Logger.instance.info("getDefaultValue | src code line = $auxText")

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
            DefaultValue(stringLiteralValue(auxText))
        } else {
            importedDefaultValue(resolver, auxText, packageName, imports)
        }
    }

    private fun stringLiteralValue(auxText: String): String {
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

    private fun importedDefaultValue(
        resolver: (pckg: String, name: String) -> ResolvedSymbol?,
        auxText: String,
        packageName: String,
        imports: List<String>
    ): DefaultValue {

        var result = auxText
        val indexOfFinalClosingParenthesis = result.indexOfFinalClosingParenthesis()
        if (indexOfFinalClosingParenthesis != null) {
            result = result.removeRange(indexOfFinalClosingParenthesis, result.length)
        }

        // ':' means its another parameter (I think.. I don't know what other meaning a ':' would have here..)
        val indexOfNextParam = result.indexOfFirst { it == ':' }.takeIf { it != -1 }

        if (result.firstParenthesisIsOpening() && // if first parenthesis is "(" then it is not closing list of function params
            result.contains("(") && // we have a "(" and it's before a ")"
            result.indexOf('(') < (indexOfNextParam ?: result.lastIndex) // "(" is before next param if it exists
        ) {
            if (indexOfNextParam != null) {
                result = result.removeRange(indexOfNextParam, result.length)
            }

            val commaIndex = result.indexOfLast { it == ',' }
            if (commaIndex != -1) {
                result = result.removeRange(commaIndex, result.length)
            }
        } else {
            val index = result.indexOfFirst { it == ' ' || it == ',' || it == ')' }
            if (index != -1)
                result = result.removeRange(index, result.length)
        }

        if (result == "true"
            || result == "false"
            || result == "null"
            || result.first().isDigit()) {
            return DefaultValue(result)
        }

        val importableAux = result.removeFromTo("(", ")")

        if (result.length - importableAux.length > 2) {
            //we detected a function call with args, we can't resolve this
            throw IllegalDestinationsSetup("Navigation arguments using function calls with parameters as their default value " +
                    "are not currently supported (near: '$auxText')")
        }

        val importable = importableAux.split(".")[0]
        val defValueImports = imports.filter { it.endsWith(".$importable") }

        if (defValueImports.isNotEmpty()) {
            return DefaultValue(result, defValueImports)
        }

        if (resolver.invoke(packageName, importable).existsAndIsAccessible()) {
            return DefaultValue(result, listOf("${packageName}.$importable"))
        }

        val wholePackageImports = imports
            .filter { it.endsWith(".*") }

        val validImports = wholePackageImports
            .filter { resolver.invoke(it.removeSuffix(".*"), importable).existsAndIsAccessible() }

        if (validImports.size == 1) {
            return DefaultValue(result, listOf(validImports[0]))
        }

        if (result.startsWith("arrayListOf(") //std kotlin lib
            || result.startsWith("arrayOf(") //std kotlin lib
        ) {
            return DefaultValue(result)
        }

        if (resolver.invoke(packageName, importable).existsAndIsPrivate()) {
            throw IllegalDestinationsSetup("Navigation arguments with default values which uses a private declaration are not currently supported (near: '$auxText')")
        }

        return DefaultValue(result, wholePackageImports)
    }
}

private fun String.firstParenthesisIsOpening(): Boolean {
    val indexOfFirstOpening = this.indexOfFirst { it == '(' }
    val indexOfFirstClosing = this.indexOfFirst { it == ')' }

    return indexOfFirstClosing >= indexOfFirstOpening
}

private fun String.indexOfFinalClosingParenthesis(): Int? {
    var closingsExpected = 0

    for (i in this.indices) {
        when (this[i]) {
            '(' -> closingsExpected++

            ')' -> if (closingsExpected > 0) {
                closingsExpected--
            } else {
                return i
            }
        }
    }

    return null
}

@OptIn(KspExperimental::class)
fun KSValueParameter.getDefaultValue(resolver: Resolver): DefaultValue? {
    if (!hasDefault) return null

    /*
        This is not ideal: having to read the first n lines of the file,
        and parse the default value manually from the source code
        I haven't found a better way yet, seems like there is no other
        way in KSP :/
    */

    if (location is NonExistLocation) {
        throw IllegalDestinationsSetup("Cannot detect default value for navigation" +
                " argument '${name!!.asString()}' because we don't have access to source code. " +
                "Are you using navArgsDelegate with code from different module?")
    }

    Logger.instance.info("getDefaultValue | name = ${name!!.asString()} type = $type")
    val fileLocation = location as FileLocation
    val (lines, imports) = File(fileLocation.filePath)
        .readLinesAndImports(fileLocation.lineNumber, fileLocation.lineNumber + 10)

    return DefaultParameterValueReader.readDefaultValue(
        resolver = { pckg, name ->
            kotlin.runCatching {
                resolver.getDeclarationsFromPackage(pckg)
                    .firstOrNull { it.simpleName.asString().contains(name) }
                    ?.let {
                        ResolvedSymbol(it.isPublic() || it.isInternal())
                    }
            }.getOrNull()
        },
        srcCodeLines = lines,
        packageName = this.containingFile!!.packageName.asString(),
        imports = imports,
        argName = name!!.asString(),
        argType = type.toString()
    ).also { Logger.instance.info("getDefaultValue | Result = $it") }
}

class ResolvedSymbol(val isAccessible: Boolean)

private fun ResolvedSymbol?.existsAndIsAccessible() = this != null && this.isAccessible
private fun ResolvedSymbol?.existsAndIsPrivate() = this != null && !this.isAccessible
