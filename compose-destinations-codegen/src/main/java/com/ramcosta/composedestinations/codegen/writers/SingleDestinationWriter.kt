package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.codeGenDestination
import com.ramcosta.composedestinations.codegen.codeGenNoArgsDestination
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.NavArgResolver
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationContentFunctionWriter

class SingleDestinationWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val core: Core,
    private val navArgResolver: NavArgResolver,
    private val destination: DestinationGeneratingParamsWithNavArgs,
    private val customNavTypeByType: Map<Type, CustomNavType>,
    private val importableHelper: ImportableHelper
) {

    private val packageName = "$codeGenBasePackageName.destinations"
    private val navArgs get() = destination.navArgs

    init {
        if (destination.navGraphInfo.start && destination.navArgs.any { it.isMandatory }) {
            throw IllegalDestinationsSetup("\"'${destination.composableName}' composable: Start destinations cannot have mandatory navigation arguments!")
        }

        importableHelper.addAll(destinationTemplate.imports)
        importableHelper.addPriorityQualifiedImport(destination.composableQualifiedName, destination.composableName)
    }

    fun write(): GeneratedDestination = with(destination) {
        codeGenerator.makeFile(
            packageName = packageName,
            name = name,
            sourceIds = sourceIds.toTypedArray()
        ).writeSourceFile(
            packageStatement = destinationTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = destinationTemplate.sourceCode
                .replace(DESTINATION_NAME, name)
                .replaceSuperclassDestination()
                .addNavArgsDataClass()
                .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, objectWideRequireOptInAnnotationsCode())
                .replace(BASE_ROUTE, destination.cleanRoute)
                .replace(NAV_ARGS_CLASS_SIMPLE_NAME, navArgsDataClassName())
                .replace(COMPOSED_ROUTE, constructRouteFieldCode())
                .replace(DESTINATION_PARENT, parentNavGraph())
                .replace(NAV_ARGUMENTS, navArgumentsDeclarationCode())
                .replace(DEEP_LINKS, deepLinksDeclarationCode())
                .replace(DESTINATION_STYLE, destinationStyle())
                .replace(CONTENT_FUNCTION_CODE, contentFunctionCode())
                .replace(ARGS_TO_DIRECTION_METHOD, invokeMethodsCode())
                .replace(ARGS_FROM_METHODS, argsFromFunctions())
        )

        return GeneratedDestination(
            sourceIds = sourceIds,
            qualifiedName = "$packageName.$name",
            simpleName = name,
            navArgsImportable = navArgsDataClassImportable()?.let {
                if (navArgsDelegateType == null) {
                    it.copy(simpleName = "NavArgs", qualifiedName = "$packageName.$name.NavArgs")
                } else {
                    it
                }
            },
            navGraphInfo = navGraphInfo,
            requireOptInAnnotationTypes = gatherOptInAnnotations()
                .filter { !it.isOptedIn }
                .map { it.importable }
                .toList(),
        )
    }

    private fun String.replaceSuperclassDestination(): String {
        if (navArgs.isEmpty()) {
            return replace(SUPERTYPE, codeGenNoArgsDestination)
        }

        val superType = if (destination.navArgsDelegateType != null) {
            "${codeGenDestination}<${destination.navArgsDelegateType.type.getCodePlaceHolder()}>"
        } else {
            "${codeGenDestination}<${destination.name}.NavArgs>"
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

    private fun gatherOptInAnnotations(): List<OptInAnnotation> {
        val optInByAnnotation = destination.requireOptInAnnotationTypes.associateWithTo(mutableMapOf()) { false }

        destination.parameters.forEach { param ->
            optInByAnnotation.putAll(
                param.type.recursiveRequireOptInAnnotations().associateWith { requireOptInType ->
                    // if the destination itself doesn't need this annotation, then it was opted in
                    !destination.requireOptInAnnotationTypes.contains(requireOptInType)
                }
            )
        }

        if (destination.destinationStyleType is DestinationStyleType.Animated) {
            optInByAnnotation.putAll(destination.destinationStyleType.requireOptInAnnotations.associateWithTo(mutableMapOf()) { false })
        }

        if (isRequiredReceiverExperimentalOptedIn() || isRequiredAnimationExperimentalOptedIn()) {
            // user has opted in, so we will too
            experimentalAnimationApiType.addImport()
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

    private fun objectWideRequireOptInAnnotationsCode(): String {
        val code = StringBuilder()
        val optInByAnnotation = gatherOptInAnnotations()

        val (optedIns, nonOptedIns) = optInByAnnotation
            .onEach { it.importable.addImport() }
            .partition { it.isOptedIn }

        nonOptedIns.forEach {
            code += "@${it.importable.getCodePlaceHolder()}\n"
        }

        if (optedIns.isNotEmpty()) {
            code += "@OptIn(${optedIns.joinToString(", ") { "${it.importable.simpleName}::class" }})\n"
        }

        return code.toString()
    }

    private fun invokeMethodsCode(): String {
        if (navArgs.isEmpty()) {

            return """
            |     
            |    operator fun invoke() = this
            |    
            """.trimMargin()
        }

        val template = """
        |
        |    override fun invoke(navArgs: %s1): $CORE_DIRECTION = with(navArgs) {
		|        invoke(%s2)
	    |    }
        |     
        |    operator fun invoke(
        |%s3
        |    ): $CORE_DIRECTION {
        |        return object : $CORE_DIRECTION {
        |            override val route = %s4
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
            .replace("%s1", navArgsDataClassName())
            .replace("%s2", navArgs.joinToString(", ") { it.name })
            .replace("%s3", innerNavArgsParametersCode())
            .replace("%s4", route)
    }

    private fun innerNavArgsParametersCode(prefixWithVal: Boolean = false): String {
        val args = StringBuilder()
        val argPrefix = if (prefixWithVal) {
            "val "
        } else ""

        navArgs.forEachIndexed { i, it ->
            args += "\t\t$argPrefix${it.name}: ${it.type.toTypeCode(importableHelper)}${defaultValueForInvokeFunction(it)},"

            if (i != navArgs.lastIndex) {
                args += "\n"
            }
        }

        return args.toString()
    }

    private fun Parameter.stringifyForNavigation(): String {
        if (isCustomTypeNavArg()) {
            val codePlaceHolder = navArgResolver.customNavTypeCode(type)

            return "$codePlaceHolder.serializeValue($name)"
        }

        if (type.importable.qualifiedName == String::class.qualifiedName) {
            return "${CORE_STRING_NAV_TYPE.getCodePlaceHolder()}.serializeValue(\"$name\", $name)"
        } else if (type.value in coreTypes.keys) {
            return "${coreTypes[type.value]!!.getCodePlaceHolder()}.serializeValue($name)"
        }

        val ifNullBeforeToString = if (type.isNullable) "?" else ""
        val ifNullSuffix = if (type.isNullable) {
            " ?: \"{${name}}\""
        } else {
            ""
        }

        return "${name}$ifNullBeforeToString${".toString()"}$ifNullSuffix"
    }

    private fun argsFromFunctions(): String = with(destination)  {
        if (navArgs.isEmpty()) {
            return ""
        }

        val argsType = navArgsDataClassName()

        return argsFromNavBackStackEntry(argsType) + "\n" + argsFromSavedStateHandle(argsType)
    }

    private fun navArgsDataClassImportable(): Importable? = with(destination) {
        return navArgsDelegateType?.type
            ?: if (navArgs.isEmpty()) {
                null
            } else {
                Importable(
                    "NavArgs",
                    "$packageName.${destination.name}.NavArgs"
                )
            }
    }

    private fun navArgsDataClassName(): String =
        navArgsDataClassImportable()?.getCodePlaceHolder() ?: "Unit"

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
            arguments += navArgResolver.resolve(destination, it)
            arguments += ","
        }

        return code.toString()
            .replace("%s2", arguments.toString())
            .prependIndent("\t")
    }

    private fun argsFromSavedStateHandle(argsType: String): String {
        val savedStateHandlePlaceholder = Importable(
            SAVED_STATE_HANDLE_SIMPLE_NAME,
            SAVED_STATE_HANDLE_QUALIFIED_NAME
        ).getCodePlaceHolder()

        val code = StringBuilder()
        code += """
                
           |override fun argsFrom(savedStateHandle: $savedStateHandlePlaceholder): $argsType {
           |    return ${argsType}(%s2
           |    )
           |}
            """.trimMargin()

        val arguments = StringBuilder()
        navArgs.forEach {
            arguments += "\n\t\t${it.name} = "
            arguments += navArgResolver.resolveFromSavedStateHandle(destination, it)
            arguments += ","
        }

        return code.toString()
            .replace("%s2", arguments.toString())
            .prependIndent("\t")
    }

    private fun defaultValueForInvokeFunction(it: Parameter): String {
        return if (it.hasDefault) " = ${it.defaultValue?.code}"
        else ""
    }

    private fun constructRouteFieldCode(): String {
        val route = constructRoute()

        return if (navArgs.isEmpty()) {
            route
        } else {
            "\"$route\""
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

        return if (args.isEmpty()) "baseRoute"
        else "\$baseRoute$mandatoryArgs$optionalArgs"
    }

    private fun contentFunctionCode(): String {
        return DestinationContentFunctionWriter(
            destination,
            navArgs,
            importableHelper
        ).write()
    }

    private fun parentNavGraph(): String {
        return when (val navGraph = destination.navGraphInfo) {
            is NavGraphInfo.Legacy -> {
                // Get the NavGraph object from its route
                importableHelper.addAndGetPlaceholder(
                    Importable(
                        navGraph.navGraphRoute,
                        "$codeGenBasePackageName.$GENERATED_NAV_GRAPHS_OBJECT.${navGraph.navGraphRoute}"
                    )
                )
            }
            is NavGraphInfo.AnnotatedSource -> {
                // Get the NavGraph object from its route too
                navGraph.graphType.toString()
                importableHelper.addAndGetPlaceholder(
                    Importable(
                        navGraph.graphType.simpleName.asNavGraphName(),
                        "$codeGenBasePackageName.$GENERATED_NAV_GRAPHS_OBJECT.${navGraph.graphType.simpleName.asNavGraphName()}"
                    )
                )
            }
        }
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
            if (it.type.isNullable) {
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
        val navDeepLinkPlaceholder = Importable(
            "navDeepLink",
            "androidx.navigation.navDeepLink"
        ).getCodePlaceHolder()

        destination.deepLinks.forEachIndexed { i, it ->
            if (i == 0) {
                code += "\n\toverride val deepLinks get() = listOf(\n\t\t"
            }

            code += "$navDeepLinkPlaceholder {\n\t\t"

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
                    val needsCustomSerializer = it.isCustomTypeNavArg() && !it.isEnumTypeOrTypeArg()
                    val hasCustomSerializer = customNavTypeByType[it.type.value]?.serializer != null
                    if (it.isMandatory && needsCustomSerializer && !hasCustomSerializer) {
                        throw IllegalDestinationsSetup(
                            "Composable '${destination.composableName}', arg name= '${it.name}': " +
                                    "deep links cannot contain mandatory navigation types of custom type unless you define" +
                                    "a custom serializer with @NavTypeSerializer. " +
                                    "This lets you control how the custom type class is defined in the string route."
                        )
                    }

                    needsCustomSerializer && !it.isMandatory && !hasCustomSerializer
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

            is DestinationStyleType.Runtime -> destinationStyleRuntime()
        }
    }

    private fun destinationStyleRuntime(): String {
        return """
                            
            private var _style: DestinationStyle? = null

            override var style: DestinationStyle
                set(value) {
                    if (value is DestinationStyle.Runtime) {
                        error("You cannot use `DestinationStyle.Runtime` other than in the `@Destination`" +
                            "annotation 'style' parameter!")
                    }
                    _style = value
                }
                get() {
                    return _style ?: error("For annotated Composables with `style = DestinationStyle.Runtime`, " +
                            "you need to explicitly set the style before calling `DestinationsNavHost`")
                }
                
        """.trimIndent()
            .prependIndent("\t")
    }

    private fun destinationStyleDialog(destinationStyleType: DestinationStyleType.Dialog): String {
        return "\n\toverride val style = ${destinationStyleType.type.importable.getCodePlaceHolder()}\n"
    }

    private fun destinationStyleAnimated(destinationStyleType: DestinationStyleType.Animated): String {
        if (core != Core.ANIMATIONS) {
            throw MissingRequiredDependency("You need to include '$CORE_ANIMATIONS_DEPENDENCY' to use $CORE_DESTINATION_ANIMATION_STYLE!")
        }

        experimentalAnimationApiType.addImport()

        if (destination.composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME) {
            Importable(
                ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME,
                ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME
            ).addImport()
        }

        return "\n\toverride val style = ${destinationStyleType.type.importable.getCodePlaceHolder()}\n"
    }

    private fun destinationStyleBottomSheet(): String {
        if (core != Core.ANIMATIONS) {
            throw MissingRequiredDependency("You need to include '$CORE_ANIMATIONS_DEPENDENCY' to use $CORE_BOTTOM_SHEET_DESTINATION_STYLE!")
        }

        return "\n\toverride val style = $CORE_BOTTOM_SHEET_DESTINATION_STYLE\n"
    }

    private fun navArgDefaultCode(param: Parameter): String = param.defaultValue.let { defaultValue ->
        if (defaultValue == null) {
            return ""
        }

        defaultValue.imports.forEach { importableHelper.addPriorityQualifiedImport(it) }

        if (defaultValue.code == "null") {
            return "\tdefaultValue = null\n\t\t"
        }

        // we always have a val with the type of the param to avoid wrong types to be inferred by kotlin
        return "\tval defValue: ${param.type.toTypeCode(importableHelper)} = ${defaultValue.code}\n\t\t" +
                "\tdefaultValue = defValue\n\t\t"
    }

    private fun Parameter.toNavTypeCode(): String {
        val coreNavTypeCode = type.toCoreNavTypeImportableOrNull()
        if (coreNavTypeCode != null) {
            return coreNavTypeCode.getCodePlaceHolder()
        }

        if (isCustomTypeNavArg()) {
            type.importable.addImport()
            type.typeArguments.forEach {
                if (it is TypeArgument.Typed) it.type.importable.addImport()
            }
            return navArgResolver.customNavTypeCode(type)
        }

        throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type ${type.importable.qualifiedName}")
    }

    private class OptInAnnotation(
        val importable: Importable,
        val isOptedIn: Boolean,
    )

    private fun Importable.getCodePlaceHolder(): String  {
        return importableHelper.addAndGetPlaceholder(this)
    }

    private fun Importable.addImport()  {
        importableHelper.addAndGetPlaceholder(this)
    }
}
