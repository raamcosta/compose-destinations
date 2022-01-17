package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationContentFunctionWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavArgResolver

class SingleDestinationWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val core: Core,
    private val navArgResolver: NavArgResolver,
    private val destination: DestinationGeneratingParamsWithNavArgs,
    private val customNavTypeByType: Map<ClassType, CustomNavType>
) {

    private val additionalImports = mutableSetOf<String>()
    private val navArgs get() = destination.navArgs

    init {
        if (destination.isStart && destination.navArgs.any { it.isMandatory }) {
            throw IllegalDestinationsSetup("\"'${destination.composableName}' composable: Start destinations cannot have mandatory navigation arguments!")
        }
    }

    fun write(): GeneratedDestination = with(destination) {
        val outputStream = codeGenerator.makeFile(
            packageName = "$codeGenBasePackageName.destinations",
            name = name,
            sourceIds = sourceIds.toTypedArray()
        )

        outputStream += destinationTemplate
            .replace(DESTINATION_NAME, name)
            .replaceSuperclassDestination()
            .addNavArgsDataClass()
            .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, objectWideRequireOptInAnnotations())
            .replace(COMPOSED_ROUTE, constructRoute())
            .replace(NAV_ARGUMENTS, navArgumentsDeclarationCode())
            .replace(DEEP_LINKS, deepLinksDeclarationCode())
            .replace(DESTINATION_STYLE, destinationStyle())
            .replace(CONTENT_FUNCTION_CODE, contentFunctionCode())
            .addInvokeWithArgsMethod()
            .replace(ARGS_FROM_METHODS, argsFromFunctions())
            .replace(ADDITIONAL_IMPORTS, additionalImports())

        outputStream.close()

        return GeneratedDestination(
            sourceIds = sourceIds,
            qualifiedName = qualifiedName,
            simpleName = name,
            isStartDestination = isStart,
            navGraphRoute = navGraphRoute,
            requireOptInAnnotationTypes = baseOptInAnnotations()
                .filter { !it.isOptedIn }
                .map { it.classType }
                .toList(),
        )
    }

    private fun String.replaceSuperclassDestination(): String {
        if (navArgs.isEmpty()) {
            return replace(SUPERTYPE, GENERATED_NO_ARGS_DESTINATION)
        }

        val superType = if (destination.navArgsDelegateType != null) {
            "${GENERATED_DESTINATION}<${destination.navArgsDelegateType.simpleName}>"
        } else {
            "${GENERATED_DESTINATION}<${destination.name}.NavArgs>"
        }

        return replace(SUPERTYPE, superType)
    }

    private fun String.addNavArgsDataClass(): String {
        if (navArgs.isEmpty() || destination.navArgsDelegateType != null) {
            return removeInstancesOf(NAV_ARGS_DATA_CLASS)
        }

        val code = StringBuilder()
        code += "\n\n"
        code += "\tdata class NavArgs(\n"
        code += "${innerNavArgsParametersCode(true)}\n"
        code += "\t)"

        return replace(NAV_ARGS_DATA_CLASS, code.toString())
    }

    private fun baseOptInAnnotations(): List<OptInAnnotation> {
        val optInByAnnotation = destination.requireOptInAnnotationTypes.associateWithTo(mutableMapOf()) { false }
        if (destination.destinationStyleType is DestinationStyleType.Animated) {
            optInByAnnotation.putAll(destination.destinationStyleType.requireOptInAnnotations.associateWithTo(mutableMapOf()) { false })
        }

        if (isRequiredReceiverExperimentalOptedIn() || isRequiredAnimationExperimentalOptedIn()) {
            // user has opted in, so we will too
            additionalImports.add(experimentalAnimationApiType.qualifiedName)
            optInByAnnotation[experimentalAnimationApiType] = true
        }

        return optInByAnnotation.map { OptInAnnotation(it.key, it.value) }
    }

    private fun isRequiredAnimationExperimentalOptedIn(): Boolean {
        return destination.destinationStyleType is DestinationStyleType.Animated
                && !destination.destinationStyleType.requireOptInAnnotations.contains(experimentalAnimationApiType)
    }

    private fun isRequiredReceiverExperimentalOptedIn(): Boolean {
        return destination.composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME
                && !destination.requireOptInAnnotationTypes.contains(experimentalAnimationApiType)
    }

    private fun objectWideRequireOptInAnnotations(): String {
        val code = StringBuilder()
        val optInByAnnotation = baseOptInAnnotations()

        optInByAnnotation.forEach {
            additionalImports.add(it.classType.qualifiedName)
            code += if (it.isOptedIn) {
                "@OptIn(${it.classType.simpleName}::class)\n"
            } else {
                "@${it.classType.simpleName}\n"
            }
        }

        return code.toString()
    }

    private fun additionalImports(): String {
        val imports = StringBuilder()

        additionalImports.add(destination.composableQualifiedName)

        if (destination.parameters.any { it.type.classType.qualifiedName == DESTINATIONS_NAVIGATOR_QUALIFIED_NAME }) {
            additionalImports.add(CORE_NAV_DESTINATIONS_NAVIGATION_QUALIFIED_NAME)
        }

        additionalImports.sorted().forEach {
            imports += "\nimport $it"
        }

        return imports.toString()
    }

    private fun String.addInvokeWithArgsMethod(): String {
        return replace(ARGS_TO_ROUTED_METHOD, invokeWithArgsMethod())
    }

    private fun invokeWithArgsMethod(): String {
        if (navArgs.isEmpty()) {

            return """
            |     
            |    operator fun invoke() = this
            |    
            """.trimMargin()
        }

        val template = """
        |     
        |    operator fun invoke(
        |%s1
        |    ): $CORE_DIRECTION {
        |        return object : $CORE_DIRECTION {
        |            override val route = %s2
        |        }
        |    }
        |    
        """.trimMargin()

        var route = "\"${constructRoute()}\""
            .replace("/", "\" + \n\t\t\t\t\t\"/")
            .replace("?", "\" + \n\t\t\t\t\t\"?")

        navArgs.forEach {
            route = route.replace("{${it.name}}", "\${${it.stringifyForNavigation()}}")
        }

        return template
            .replace("%s1", innerNavArgsParametersCode())
            .replace("%s2", route)
    }

    private fun innerNavArgsParametersCode(prefixWithVal: Boolean = false): String {
        val args = StringBuilder()
        val argPrefix = if (prefixWithVal) {
            "val "
        } else ""

        navArgs.forEachIndexed { i, it ->
            args += "\t\t$argPrefix${it.name}: ${it.type.classType.simpleName}${if (it.isNullable) "?" else ""}${defaultValueForWithArgsFunction(it)},"

            if (i != navArgs.lastIndex) {
                args += "\n"
            }
        }

        return args.toString()
    }

    private fun Parameter.stringifyForNavigation(): String {
        if (isComplexTypeNavArg()) {
            val navTypeName = customNavTypeByType[type.classType]!!.name
            additionalImports.add("$codeGenBasePackageName.navtype.$navTypeName")

            val (ifNullPrefix, ifNullSuffix) = if (isNullable) {
                "$name?.let { " to " } ?: \"{${name}}\""
            } else {
                "" to ""
            }
            return "$ifNullPrefix$navTypeName.serializeValue($name, $isMandatory)$ifNullSuffix"
        }

        val ifNullSuffix = if (isNullable) {
            " ?: \"{${name}}\""
        } else {
            ""
        }

        if (type.classType.simpleName == "String") {
            return "$CORE_STRING_NAV_TYPE.serializeValue($name, $isMandatory)$ifNullSuffix"
        }

        val ifNullBeforeToString = if (isNullable) "?" else ""
        return "${name}$ifNullBeforeToString${".toString()"}$ifNullSuffix"
    }

    private fun argsFromFunctions(): String = with(destination)  {
        if (navArgs.isEmpty()) {
            return ""
        }

        val argsType = if (navArgsDelegateType == null) {
            "NavArgs"
        } else {
            additionalImports.add(navArgsDelegateType.qualifiedName)
            navArgsDelegateType.simpleName
        }

        return argsFromNavBackStackEntry(argsType) + "\n" + argsFromSavedStateHandle(argsType)
    }

    private fun argsFromNavBackStackEntry(argsType: String): String {
        val code = StringBuilder()
        code += """
                
           |override fun argsFrom(navBackStackEntry: $NAV_BACK_STACK_ENTRY_SIMPLE_NAME): $argsType {
           |    return ${argsType}(%s2
           |    )
           |}
            """.trimMargin()

        val arguments = StringBuilder()
        navArgs.forEach {
            arguments += "\n\t\t${it.name} = "
            arguments += navArgResolver.resolve(destination, additionalImports, it)
            arguments += ","
        }

        return code.toString()
            .replace("%s2", arguments.toString())
            .prependIndent("\t")
    }

    private fun argsFromSavedStateHandle(argsType: String): String {
        additionalImports.add(SAVED_STATE_HANDLE_QUALIFIED_NAME)

        val code = StringBuilder()
        code += """
                
           |override fun argsFrom(savedStateHandle: $SAVED_STATE_HANDLE_SIMPLE_NAME): $argsType {
           |    return ${argsType}(%s2
           |    )
           |}
            """.trimMargin()

        val arguments = StringBuilder()
        navArgs.forEach {
            arguments += "\n\t\t${it.name} = "
            arguments += navArgResolver.resolveFromSavedStateHandle(destination, additionalImports, it)
            arguments += ","
        }

        return code.toString()
            .replace("%s2", arguments.toString())
            .prependIndent("\t")
    }

    private fun defaultValueForWithArgsFunction(it: Parameter): String {
        return when {
            it.hasDefault -> " = ${it.defaultValue?.code}"

            it.isNullable -> " = null"

            else -> ""

        }
    }

    private fun constructRoute(args: List<Parameter> = navArgs): String {
        val mandatoryArgs = StringBuilder()
        val optionalArgs = StringBuilder()
        args.forEach {
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

            val toNavTypeCode = it.toNavTypeCode()
            code += "navArgument(\"${it.name}\") {\n\t\t\t"
            code += "type = $toNavTypeCode\n\t\t"
            if (it.isNullable) {
                if (toNavTypeCode != CORE_STRING_NAV_TYPE && !it.isComplexTypeNavArg()) {
                    throw IllegalDestinationsSetup("Composable '${destination.composableName}', argument '${it.name}': Only String, Parcelable, Serializable and Enum navigation arguments can be nullable")
                }
                code += "\tnullable = true\n\t\t"
            }
            code += navArgDefaultCode(it)
            code += "}"

            code += if (i != navArgs.lastIndex) {
                ",\n\t\t"
            } else {
                "\n\t)\n"
            }
        }

        return code.toString()
    }

    private fun deepLinksDeclarationCode(): String {
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
                val uriPattern = if (it.uriPattern.contains(DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER)) {
                    if (it.uriPattern.endsWith(DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER)) {
                        it.uriPattern.replace(DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER, constructRouteForDeepLinkPlaceholder())
                    } else {
                        throw IllegalDestinationsSetup("Composable '${destination.composableName}': deep link usage of 'FULL_ROUTE_PLACEHOLDER' must be as a suffix")
                    }
                } else {
                    it.uriPattern
                }
                code += "\turiPattern = \"$uriPattern\"\n\t\t"
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

    private fun constructRouteForDeepLinkPlaceholder(): String {
        val args = navArgs
            .toMutableList()
            .apply {
                removeAll {
                    val isComplexType = it.isComplexTypeNavArg()
                    val hasCustomSerializer = customNavTypeByType[it.type.classType]?.serializer != null
                    if (it.isMandatory && isComplexType && !hasCustomSerializer) {
                        throw IllegalDestinationsSetup(
                            "Composable '${destination.composableName}', arg name= '${it.name}': " +
                                    "deep links cannot contain mandatory navigation types of complex type unless you define" +
                                    "a custom serializer with @NavTypeSerializer." +
                                    "This lets you control how the complex type class is defined in the string route."
                        )
                    }

                    isComplexType && !it.isMandatory && !hasCustomSerializer
                }
            }

        return constructRoute(args)
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
        additionalImports.add(destinationStyleType.type.classType.qualifiedName)

        return "\n\toverride val style = ${destinationStyleType.type.classType.simpleName}\n"
    }

    private fun destinationStyleAnimated(destinationStyleType: DestinationStyleType.Animated): String {
        if (core != Core.ANIMATIONS) {
            throw MissingRequiredDependency("You need to include '$CORE_ANIMATIONS_DEPENDENCY' to use $CORE_DESTINATION_ANIMATION_STYLE!")
        }

        additionalImports.add(experimentalAnimationApiType.qualifiedName)
        additionalImports.add(destinationStyleType.type.classType.qualifiedName)

        if (destination.composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME) {
            additionalImports.add(ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME)
        }

        return "\n\toverride val style = ${destinationStyleType.type.classType.simpleName}\n"
    }

    private fun destinationStyleBottomSheet(): String {
        if (core != Core.ANIMATIONS) {
            throw MissingRequiredDependency("You need to include '$CORE_ANIMATIONS_DEPENDENCY' to use $CORE_BOTTOM_SHEET_DESTINATION_STYLE!")
        }

        additionalImports.add("$CORE_PACKAGE_NAME.spec.DestinationStyle")
        return "\n\toverride val style = $CORE_BOTTOM_SHEET_DESTINATION_STYLE\n"
    }

    private fun navArgDefaultCode(param: Parameter): String = param.defaultValue.let { defaultValue ->
        if (defaultValue == null) {
            return ""
        }

        if (defaultValue.code == "null") {
            return "\tdefaultValue = null\n\t\t"
        }

        if (param.type.isEnum) {
            return "\tdefaultValue = ${defaultValue.code}.toString()\n\t\t"
        }

        return "\tdefaultValue = ${defaultValue.code}\n\t\t"
    }

    private fun Parameter.toNavTypeCode(): String {
        val primitiveNavTypeCode = type.toPrimitiveNavTypeCodeOrNull()
        if (primitiveNavTypeCode != null) {
            return primitiveNavTypeCode
        }

        if (isComplexTypeNavArg()) {
            additionalImports.add(type.classType.qualifiedName)
            return customNavTypeByType[type.classType]!!.name
        }

        if (type.isEnum) {
            additionalImports.add(type.classType.qualifiedName)
            return CORE_STRING_NAV_TYPE
        }

        throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type ${type.classType.qualifiedName}")
    }

    private class OptInAnnotation(
        val classType: ClassType,
        val isOptedIn: Boolean,
    )
}