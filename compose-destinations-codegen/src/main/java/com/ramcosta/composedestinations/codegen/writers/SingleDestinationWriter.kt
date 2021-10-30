package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationContentFunctionWriter

class SingleDestinationWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val availableDependencies: AvailableDependencies,
    private val destination: Destination
) {

    private val additionalImports = mutableSetOf<String>()
    private val navArgs = getNavArgs()

    fun write(): GeneratedDestination = with(destination) {
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
            .replace(ARGS_FROM_METHODS, argsFromFunctions())
            .replace(ADDITIONAL_IMPORTS, additionalImports())

        outputStream.close()

        return GeneratedDestination(
            sourceIds = sourceIds,
            qualifiedName = qualifiedName,
            simpleName = name,
            isStartDestination = isStart,
            navGraphRoute = navGraphRoute,
            requireOptInAnnotationNames = baseOptInAnnotations().filter { !it.isOptedIn }.map { it.annotationName }.toList(),
        )
    }

    private fun getNavArgs(): List<Parameter> {
        return if (destination.navArgsDelegateType == null) {
            destination.parameters.filter { it.type.toNavTypeCodeOrNull() != null }
        } else {
            if (destination.navArgsDelegateType.navArgs.any { it.type.toNavTypeCodeOrNull() == null }) {
                throw IllegalDestinationsSetup("Composable ${destination.composableName}: '$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT' cannot have arguments that are not navigation types.")
            }

            if (destination.parameters.any { it.type.toNavTypeCodeOrNull() != null }) {
                throw IllegalDestinationsSetup("Composable ${destination.composableName}: annotated function cannot define arguments of navigation type if using a '$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT' class.")
            }

            destination.navArgsDelegateType.navArgs
        }
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
        val replaceUnknownOrNullableArgs = StringBuilder()
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
            args += "\t\t${it.name}: ${it.type.simpleName}${if (it.type.isNullable) "?" else ""}${defaultValueForWithArgsFunction(it)},"

            if (it.type.isNullable) {
                replaceUnknownOrNullableArgs += """
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
                    replaceUnknownOrNullableArgs += "\n"
                } else {
                    replace += "\n"
                }
            }
        }

        return template
            .replace("%s1", args.toString())
            .replace("%s2", replaceUnknownOrNullableArgs.toString())
            .replace("%s3", replace.toString())
    }

    private fun argsFromFunctions(): String {
        if (destination.navArgsDelegateType == null) {
            return ""
        }

        return argsFromNavBackStackEntry() + "\n" + argsFromSavedStateHandle()
    }

    private fun argsFromNavBackStackEntry(): String = with(destination) {
        if (navArgsDelegateType == null) {
            return ""
        }

        additionalImports.add(navArgsDelegateType.qualifiedName)

        val code = StringBuilder()
        code += """
                
           |fun argsFrom(navBackStackEntry: $NAV_BACK_STACK_ENTRY_SIMPLE_NAME): ${navArgsDelegateType.simpleName} {
           |    return ${navArgsDelegateType.simpleName}(%s2
           |    )
           |}
            """.trimMargin()

        val arguments = StringBuilder()
        navArgs.forEach {
            arguments += "\n\t\t${it.name} = "
            arguments += DestinationContentFunctionWriter.resolveNavArg(destination,
                additionalImports,
                it)
            arguments += ","
        }

        return code.toString()
            .replace("%s2", arguments.toString())
            .prependIndent("\t")
    }

    private fun argsFromSavedStateHandle(): String = with(destination) {
        if (navArgsDelegateType == null) {
            return ""
        }

        additionalImports.add(navArgsDelegateType.qualifiedName)
        additionalImports.add(SAVED_STATE_HANDLE_QUALIFIED_NAME)

        val code = StringBuilder()
        code += """
                
           |fun argsFrom(savedStateHandle: $SAVED_STATE_HANDLE_SIMPLE_NAME): ${navArgsDelegateType.simpleName} {
           |    return ${navArgsDelegateType.simpleName}(%s2
           |    )
           |}
            """.trimMargin()

        val arguments = StringBuilder()
        navArgs.forEach {
            arguments += "\n\t\t${it.name} = "
            arguments += DestinationContentFunctionWriter.resolveNavArgFromSavedStateHandle(destination,
                additionalImports,
                it)
            arguments += ","
        }

        return code.toString()
            .replace("%s2", arguments.toString())
            .prependIndent("\t")
    }

    private fun defaultValueForWithArgsFunction(it: Parameter): String {
        return when {
            it.hasDefault -> " = ${it.defaultValue?.code}"

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

    private fun contentFunctionCode(): String {
        return DestinationContentFunctionWriter(
            destination,
            navArgs,
            additionalImports
        ).write()
    }

    private fun navArgumentsDeclarationCode(): String {
        val code = StringBuilder()

        navArgs.forEachIndexed { i, it ->
            if (i == 0) {
                code += "\n\toverride val arguments get() = listOf(\n\t\t"
            }

            code += "navArgument(\"${it.name}\") {\n\t\t\t"
            code += "type = ${it.toNavTypeCode()}\n\t\t\t"
            if (it.type.isNullable) {
                code += "nullable = true\n\t\t"
            }
            code += navArgDefaultCode(it.defaultValue)
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
                code += "\n\toverride val deepLinks get() = listOf(\n\t\t"
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

    private fun navArgDefaultCode(argDefault: DefaultValue?): String {
        return if (argDefault != null) "\tdefaultValue = ${argDefault.code}\n\t\t" else ""
    }

    private fun Parameter.toNavTypeCode(): String {
        return type.toNavTypeCodeOrNull() ?: throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type ${type.qualifiedName}")
    }

    private class OptInAnnotation(
        val annotationName: String,
        val isOptedIn: Boolean
    )
}