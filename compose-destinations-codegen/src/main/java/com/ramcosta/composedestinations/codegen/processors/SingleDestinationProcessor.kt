package com.ramcosta.composedestinations.codegen.processors

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.templates.COMPOSED_ROUTE
import com.ramcosta.composedestinations.codegen.templates.CONTENT_FUNCTION_CODE
import com.ramcosta.composedestinations.codegen.templates.DESTINATION_NAME
import com.ramcosta.composedestinations.codegen.templates.NAV_ARGUMENTS
import com.ramcosta.composedestinations.codegen.templates.ADDITIONAL_IMPORTS
import com.ramcosta.composedestinations.codegen.templates.WITH_ARGS_METHOD
import com.ramcosta.composedestinations.codegen.templates.destinationTemplate

class SingleDestinationProcessor(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val availableDependencies: AvailableDependencies,
    private val destination: Destination
) {

    private val additionalImports = mutableSetOf<String>()
    private val navArgs = destination.parameters.filter { it.type.toNavTypeCodeOrNull() != null }

    fun process(): GeneratedDestination = with(destination) {
        if (isStart && navArgs.any { it.isMandatory }) {
            throw IllegalDestinationsSetup("Start destinations cannot have mandatory navigation arguments! (route: \"$cleanRoute\")")
        }

        val outputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = name,
            sourceIds = sourceIds.toTypedArray()
        )

        val composedRoute = constructRoute()
        outputStream += destinationTemplate
            .replace(DESTINATION_NAME, name)
            .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, objectWideRequireOptInAnnotations())
            .replace(COMPOSED_ROUTE, composedRoute)
            .replace(NAV_ARGUMENTS, navArgumentsDeclarationCode())
            .replace(DEEP_LINKS, deepLinksDeclarationCode(composedRoute))
            .replace(DESTINATION_STYLE, destinationStyle())
            .replace(CONTENT_FUNCTION_CODE, contentFunctionCode())
            .replace(WITH_ARGS_METHOD, withArgsMethod())
            .replace(ADDITIONAL_IMPORTS, additionalImports())

        outputStream.close()

        return GeneratedDestination(
            sourceIds = sourceIds,
            qualifiedName = qualifiedName,
            simpleName = name,
            isStartDestination = isStart,
            navGraphRoute = navGraphRoute,
            requireOptInAnnotationNames = baseOptInAnnotations().filter { !it.isOptedIn }.map { it.annotationName }.toList(),
            isBottomSheetStyle = destination.destinationStyleType is DestinationStyleType.BottomSheet
        )
    }

    private fun baseOptInAnnotations(): List<OptInAnnotation> {
        val optInByAnnotation = destination.requireOptInAnnotationNames.associateWithTo(mutableMapOf()) { false }
        if (destination.destinationStyleType is DestinationStyleType.Animated) {
            optInByAnnotation.putAll(destination.destinationStyleType.requireOptInAnnotations.associateWithTo(mutableMapOf()) { false })
        }

        if (isRequiredReceiverExperimentalOptedIn() || isRequiredAnimationExperimentalOptedIn()) {
            // user has opted in, so we will too
            additionalImports.add(EXPERIMENTAL_ANIMATION_API_QUALIFIED_NAME)
            optInByAnnotation[EXPERIMENTAL_ANIMATION_API_SIMPLE_NAME] = true
        }

        return optInByAnnotation.map { OptInAnnotation(it.key, it.value) }
    }

    private fun isRequiredAnimationExperimentalOptedIn(): Boolean {
        return destination.destinationStyleType is DestinationStyleType.Animated
                && !destination.destinationStyleType.requireOptInAnnotations.contains(EXPERIMENTAL_ANIMATION_API_SIMPLE_NAME)
    }

    private fun isRequiredReceiverExperimentalOptedIn(): Boolean {
        return destination.composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME
                && !destination.requireOptInAnnotationNames.contains(EXPERIMENTAL_ANIMATION_API_SIMPLE_NAME)
    }

    private fun objectWideRequireOptInAnnotations(): String {
        val code = StringBuilder()
        val optInByAnnotation = baseOptInAnnotations()

        optInByAnnotation.forEach {
            code += if (it.isOptedIn) {
                "@OptIn(${it.annotationName}::class)\n"
            } else {
                "@${it.annotationName}\n"
            }
        }

        return code.toString()
    }

    private fun additionalImports(): String {
        val imports = StringBuilder()

        additionalImports.add(destination.composableQualifiedName)

        if (destination.parameters.any { it.type.qualifiedName == DESTINATIONS_NAVIGATOR_QUALIFIED_NAME }) {
            additionalImports.add(CORE_NAV_DESTINATIONS_NAVIGATION_QUALIFIED_NAME)
        }

        additionalImports.sorted().forEach {
            imports += "\nimport $it"
        }

        return imports.toString()
    }

    private fun withArgsMethod(): String {
        if (navArgs.isEmpty()) return ""

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

        navArgs.forEachIndexed { i, it ->
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


            if (i != navArgs.lastIndex) {
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
            it.hasDefault -> {
                " = ${it.defaultValueSrc}"
            }

            it.type.isNullable -> " = null"

            else -> ""

        }
    }

    private fun constructRoute(): String {
        val mandatoryArgs = StringBuilder()
        val optionalArgs = StringBuilder()
        navArgs.forEach {
            if (it.isMandatory) {
                mandatoryArgs += "/{${it.name}}"
            } else {
                optionalArgs += "?${it.name}={${it.name}}"
            }
        }

        return destination.cleanRoute + mandatoryArgs.toString() + optionalArgs.toString()
    }

    private fun contentFunctionCode(): String = with(destination) {
        val receiver = when (composableReceiverSimpleName) {
            ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME -> {
                additionalImports.add(ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME)
                "val animatedVisibilityScope = situationalParameters[$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME::class.java] as? $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME ?: ${GeneratedExceptions.MISSING_VISIBILITY_SCOPE}" +
                        "\n\t\tanimatedVisibilityScope."
            }
            COLUMN_SCOPE_SIMPLE_NAME -> {
                additionalImports.add("androidx.compose.foundation.layout.$COLUMN_SCOPE_SIMPLE_NAME")
                "val columnScope = situationalParameters[$COLUMN_SCOPE_SIMPLE_NAME::class.java] as? $COLUMN_SCOPE_SIMPLE_NAME ?: ${GeneratedExceptions.MISSING_COLUMN_SCOPE}" +
                        "\n\t\tcolumnScope."
            }
            else -> {
                ""
            }
        }
        return "$receiver${composableName}(${prepareArguments()})"
    }

    private fun prepareArguments(): String = with(destination) {
        var argsCode = ""

        parameters.forEachIndexed { i, it ->
            val argumentResolver = resolveArgumentForTypeAndName(it)

            if (argumentResolver != null) {
                if (i != 0) argsCode += ", "

                argsCode += "\n\t\t\t${it.name} = $argumentResolver"

            } else if (!it.hasDefault) {
                throw IllegalDestinationsSetup("Unresolvable argument type without default value: $it")
            }

            if (i == parameters.lastIndex) argsCode += "\n\t\t"
        }

        return argsCode
    }

    private fun resolveArgumentForTypeAndName(parameter: Parameter): String? {
        return when (parameter.type.qualifiedName) {
            NAV_CONTROLLER_QUALIFIED_NAME -> "navController"
            DESTINATIONS_NAVIGATOR_QUALIFIED_NAME -> "$CORE_NAV_DESTINATIONS_NAVIGATION(navController)"
            NAV_BACK_STACK_ENTRY_QUALIFIED_NAME -> "navBackStackEntry"
            SCAFFOLD_STATE_QUALIFIED_NAME -> {
                additionalImports.add("androidx.compose.material.ScaffoldState")
                "situationalParameters[ScaffoldState::class.java] as? ScaffoldState ?: ${GeneratedExceptions.SCAFFOLD_STATE_MISSING}"
            }
            else -> {
                if (navArgs.contains(parameter)) {
                    "navBackStackEntry.arguments?.${parameter.type.toNavBackStackEntryArgGetter(parameter.name)}${defaultCodeIfArgNotPresent(parameter)}"
                } else null
            }
        }
    }

    private fun defaultCodeIfArgNotPresent(parameter: Parameter): String {

        if (parameter.hasDefault) {
            return if (parameter.defaultValueSrc == "null") {
                ""
            } else " ?: ${parameter.defaultValueSrc}"
        }

        if (parameter.type.isNullable) {
            return ""
        }

        return " ?: ${GeneratedExceptions.missingMandatoryArgument(parameter.name)}"
    }

    private fun navArgumentsDeclarationCode(): String {
        val code = StringBuilder()

        navArgs.forEachIndexed { i, it ->
            if (i == 0) {
                code += "\n\toverride val arguments = listOf(\n\t\t"
            }

            code += "navArgument(\"${it.name}\") {\n\t\t\t"
            code += "type = ${it.type.toNavTypeCode()}\n\t\t\t"
            code += "nullable = ${it.type.isNullable}\n\t\t"
            code += navArgDefaultCode(it.defaultValueSrc)
            code += "}"

            code += if (i != navArgs.lastIndex) {
                ",\n\t\t"
            } else {
                "\n\t)\n"
            }
        }

        return code.toString()
    }

    private fun deepLinksDeclarationCode(composedRoute: String): String {
        val code = StringBuilder()

        destination.deepLinks.forEachIndexed { i, it ->
            if (i == 0) {
                additionalImports.add("androidx.navigation.navDeepLink")
                code += "\n\toverride val deepLinks = listOf(\n\t\t"
            }

            code += "navDeepLink {\n\t\t"

            if (it.action.isNotEmpty()) {
                code += "\taction = \"${it.action}\"\n\t\t"
            }
            if (it.mimeType.isNotEmpty()) {
                code += "\tmimeType = \"${it.mimeType}\"\n\t\t"
            }
            if (it.uriPattern.isNotEmpty()) {
                code += "\turiPattern = \"${it.uriPattern.replace(DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER, composedRoute)}\"\n\t\t"
            }
            code += "}"

            code += if (i != destination.deepLinks.lastIndex) {
                ",\n\t\t"
            } else {
                "\n\t)\n"
            }
        }

        return code.toString()
    }

    private fun destinationStyle(): String {
        return when (destination.destinationStyleType) {
            is DestinationStyleType.Default -> ""

            is DestinationStyleType.BottomSheet -> destinationStyleBottomSheet()

            is DestinationStyleType.Animated -> destinationStyleAnimated(destination.destinationStyleType)

            is DestinationStyleType.Dialog -> destinationStyleDialog(destination.destinationStyleType)
        }
    }

    private fun destinationStyleDialog(destinationStyleType: DestinationStyleType.Dialog): String {
        additionalImports.add(destinationStyleType.type.qualifiedName)

        return "\n\toverride val style = ${destinationStyleType.type.simpleName}\n"
    }

    private fun destinationStyleAnimated(destinationStyleType: DestinationStyleType.Animated): String {
        if (!availableDependencies.accompanistAnimation) {
            throw MissingRequiredDependency("You need to include '$ACCOMPANIST_NAVIGATION_ANIMATION' to use $GENERATED_ANIMATED_DESTINATION_STYLE!")
        }

        additionalImports.add(EXPERIMENTAL_ANIMATION_API_QUALIFIED_NAME)
        additionalImports.add(destinationStyleType.type.qualifiedName)

        if (destination.composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME) {
            additionalImports.add(ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME)
        }

        return "\n\toverride val style = ${destinationStyleType.type.simpleName}\n"
    }

    private fun destinationStyleBottomSheet(): String {
        if (!availableDependencies.accompanistMaterial) {
            throw MissingRequiredDependency("You need to include '$ACCOMPANIST_NAVIGATION_MATERIAL' to use $CORE_BOTTOM_SHEET_DESTINATION_STYLE!")
        }

        additionalImports.add("$PACKAGE_NAME.spec.DestinationStyle")
        return "\n\toverride val style = $CORE_BOTTOM_SHEET_DESTINATION_STYLE\n"
    }

    private fun navArgDefaultCode(argDefault: String?): String {
        return if (argDefault != null) "\tdefaultValue = ${argDefault}\n\t\t" else ""
    }

    private fun Type.toNavBackStackEntryArgGetter(argName: String): String {
        return when (qualifiedName) {
            String::class.qualifiedName -> "getString(\"$argName\")"
            Int::class.qualifiedName -> "getInt(\"$argName\")"
            Float::class.qualifiedName -> "getFloat(\"$argName\")"
            Long::class.qualifiedName -> "getLong(\"$argName\")"
            Boolean::class.qualifiedName -> "getBoolean(\"$argName\")"
            else -> throw IllegalDestinationsSetup("Unknown type $qualifiedName")
        }
    }

    private fun Type.toNavTypeCode(): String {
        return toNavTypeCodeOrNull() ?: throw IllegalDestinationsSetup("Unknown type $qualifiedName")
    }

    private fun Type.toNavTypeCodeOrNull(): String? {
        return when (qualifiedName) {
            String::class.qualifiedName -> "NavType.StringType"
            Int::class.qualifiedName -> "NavType.IntType"
            Float::class.qualifiedName -> "NavType.FloatType"
            Long::class.qualifiedName -> "NavType.LongType"
            Boolean::class.qualifiedName -> "NavType.BoolType"
            else -> null
        }
    }

    private class OptInAnnotation(
        val annotationName: String,
        val isOptedIn: Boolean
    )
}