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
                .replace(WITH_ARGS_METHOD, withArgsMethod(destination))

            outputStream.close()
        }

        return generatedFiles
    }

    private fun withArgsMethod(destination: Destination): String {
        if (destination.navParameters.isEmpty()) return ""

        val args = StringBuilder()
        val replaceNullableArgs = StringBuilder()
        val replace = StringBuilder()

        val template = """
        |     
        |    fun withArgs(
        |%s1
        |    ): String {
        |        var route = route
        |%s2
        |        return route
        |%s3
        |    }
        |    
        """.trimMargin()

        destination.navParameters.forEachIndexed { i, it ->
            args += "\t\t${it.name}: ${it.type.simpleName}${if (it.type.isNullable) "?" else ""}${defaultValueWithArgs(it)},"

            if (it.type.isNullable) {
                replaceNullableArgs += """
                   |        if (${it.name} != null) {
                   |            route = route.replace("{${it.name}}", ${it.name}${if (it.type.simpleName == "String") "" else ".toString()"})
                   |        }
                    """.trimMargin()
            } else {
                replace += "\t\t\t.replace(\"{${it.name}}\", ${it.name}${if (it.type.simpleName == "String") "" else ".toString()"})"
            }


            if (i != destination.navParameters.lastIndex) {
                args += "\n"
                if (it.type.isNullable) {
                    replaceNullableArgs += "\n"
                } else {
                    replace += "\n"
                }
            }
        }

        return template
            .replace("%s1", args.toString())
            .replace("%s2", replaceNullableArgs.toString())
            .replace("%s3", replace.toString())
    }

    private fun defaultValueWithArgs(it: Parameter): String {
        return when {
            it.defaultValue is DefaultValue.Known -> {
                " = ${it.defaultValue.srcCode}"
            }

            it.type.isNullable -> " = null"

            else -> ""

        }
    }


    private fun Destination.constructRoute(): String {
        val mandatoryArgs = StringBuilder()
        val optionalArgs = StringBuilder()
        navParameters.forEach {
            if (it.isMandatory) {
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
            Float::class.qualifiedName -> "getFloat(\"$argName\")"
            Long::class.qualifiedName -> "getLong(\"$argName\")"
            Boolean::class.qualifiedName -> "getBoolean(\"$argName\")"
            else -> throw RuntimeException("Unknown type $qualifiedName")
        }
    }

    private fun Type.toNavTypeCode(): String {
        return when (qualifiedName) {
            String::class.qualifiedName -> "NavType.StringType"
            Int::class.qualifiedName -> "NavType.IntType"
            Float::class.qualifiedName -> "NavType.FloatType"
            Long::class.qualifiedName -> "NavType.LongType"
            Boolean::class.qualifiedName -> "NavType.BoolType"
            else -> throw RuntimeException("Unknown type $qualifiedName")
        }
    }
}