package com.ramcosta.composedestinations.processors

import com.ramcosta.composedestinations.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.facades.Logger
import com.ramcosta.composedestinations.model.*
import com.ramcosta.composedestinations.templates.*
import com.ramcosta.composedestinations.utils.NAV_BACK_STACK_ENTRY_QUALIFIED_NAME
import com.ramcosta.composedestinations.utils.NAV_CONTROLLER_QUALIFIED_NAME
import com.ramcosta.composedestinations.utils.PACKAGE_NAME
import com.ramcosta.composedestinations.utils.plusAssign

internal class DestinationsProcessor(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger
) {

    fun process(destinations: Sequence<Destination>): List<GeneratedDestinationFile> {
        val generatedFiles = mutableListOf<GeneratedDestinationFile>()

        destinations.forEach { destination ->
            val fileName = destination.name
            generatedFiles.add(GeneratedDestinationFile(destination.qualifiedName, destination.name))

            val outputStream = codeGenerator.makeFile(
                packageName = PACKAGE_NAME,
                name = fileName
            )

            outputStream += destinationTemplate
                .replace(SYMBOL_QUALIFIED_NAME, destination.composableQualifiedName)
                .replace(DESTINATION_NAME, fileName)
                .replace(COMPOSED_ROUTE, destination.constructRoute())
                .replace(NAV_ARGUMENTS, navArgumentsDeclarationCode(destination))
                .replace(CONTENT_FUNCION_CODE, callActualComposable(destination))

            outputStream.close()
        }

        return generatedFiles
    }


    private fun Destination.constructRoute(): String {
        val mandatoryArgs = StringBuilder()
        val optionalArgs = StringBuilder()
        navParameters.forEach {
            val isMandatory = !it.type.isNullable && it.defaultValue is DefaultValue.None

            if (isMandatory) {
                mandatoryArgs.append("/{${it.name}}")
            } else {
                optionalArgs.append("?${it.name}={${it.name}}")
            }
        }

        return cleanRoute + mandatoryArgs.toString() + optionalArgs.toString()
    }

    private fun callActualComposable(destination: Destination): String {
        val parameters = destination.navParameters.toMutableList()
        if (destination.navBackStackEntry != null)
            parameters.add(0, destination.navBackStackEntry)

        if (destination.navController != null)
            parameters.add(0, destination.navController)

        return "${destination.composableName}(${prepareArguments(parameters)})"
    }

    private fun prepareArguments(parameters: List<Parameter>): String {
        var argsCode = ""

        parameters.forEachIndexed { i, it ->
            if (i != 0) argsCode += ", "

            argsCode += "\n\t\t\t${it.name} = ${resolveArgumentForTypeAndName(it)}"

            if (i == parameters.lastIndex) argsCode += "\n\t\t"
        }

        return argsCode
    }

    private fun resolveArgumentForTypeAndName(parameter: Parameter): String {
        return when (parameter.type.qualifiedName) {
            NAV_CONTROLLER_QUALIFIED_NAME -> "navController" //part of the normal Compose arguments, so just return it
            NAV_BACK_STACK_ENTRY_QUALIFIED_NAME -> "navBackStackEntry" //part of the normal Compose arguments, so just return it
            else -> "navBackStackEntry.arguments?.${parameter.type.toNavBackStackEntryArgGetter(parameter.name)}${defaultCodeIfArgNotPresent(parameter)}"
        }
    }

    private fun defaultCodeIfArgNotPresent(parameter: Parameter): String {

        if (parameter.defaultValue is DefaultValue.Known) {
            return if (parameter.defaultValue.srcCode == "null") {
                ""
            } else " ?: ${parameter.defaultValue.srcCode}"
        }

        if (parameter.type.isNullable) {
            return ""
        }

        return " ?: throw IllegalArgumentException(\"'${parameter.name}' argument is mandatory, but was not present!\")"
    }

    private fun navArgumentsDeclarationCode(destination: Destination): String {
        val code = StringBuilder()

        destination.navParameters.forEachIndexed { i, it ->
            if (i == 0) {
                code.append("\n\toverride val arguments = listOf(\n\t\t")
            }

            code.append("navArgument(\"${it.name}\") {\n\t\t\t")
            code.append("type = ${it.type.toNavTypeCode()}\n\t\t\t")
            code.append("nullable = ${it.type.isNullable}\n\t\t")
            code.append(navArgDefaultCode(it.defaultValue))
            code.append("}")

            if (i != destination.navParameters.lastIndex) {
                code.append(",\n\t\t")
            } else {
                code.append("\n\t)\n")
            }
        }

        return code.toString()
    }

    private fun navArgDefaultCode(argDefault: DefaultValue): String {
        return if (argDefault is DefaultValue.Known) "\tdefaultValue = ${argDefault.srcCode}\n\t\t" else ""
    }

    private fun Type.toNavBackStackEntryArgGetter(argName: String): String {
        return when (qualifiedName) {
            String::class.qualifiedName -> "getString(\"$argName\")"
            Int::class.qualifiedName -> "getInt(\"$argName\")"
            else -> throw RuntimeException("Unknown type $qualifiedName")
        }
    }

    private fun Type.toNavTypeCode(): String {
        return when (qualifiedName) {
            String::class.qualifiedName -> "NavType.StringType"
            Int::class.qualifiedName -> "NavType.IntType"
            else -> throw RuntimeException("Unknown type $qualifiedName")
        }
    }
}