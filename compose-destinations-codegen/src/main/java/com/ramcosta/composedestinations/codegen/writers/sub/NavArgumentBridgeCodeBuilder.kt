package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION
import com.ramcosta.composedestinations.codegen.commons.CORE_STRING_NAV_TYPE
import com.ramcosta.composedestinations.codegen.commons.DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.bundleImportable
import com.ramcosta.composedestinations.codegen.commons.coreTypes
import com.ramcosta.composedestinations.codegen.commons.isCustomTypeNavArg
import com.ramcosta.composedestinations.codegen.commons.isEnumTypeOrTypeArg
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.savedStateHandleImportable
import com.ramcosta.composedestinations.codegen.commons.toCoreNavTypeImportableOrNull
import com.ramcosta.composedestinations.codegen.commons.toTypeCode
import com.ramcosta.composedestinations.codegen.model.DeepLink
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.NavArgResolver

class NavArgumentBridgeCodeBuilder(
    private val importableHelper: ImportableHelper,
    private val navArgResolver: NavArgResolver,
    private val navArgs: List<Parameter>,
    private val errorLocationPrefix: String,
) {

    private val navArgumentImportable = Importable("navArgument", "androidx.navigation.navArgument")
    val navClassArgumentImportable = Importable("NamedNavArgument", "androidx.navigation.NamedNavArgument")

    private val navDeepLinkImportable = Importable("navDeepLink", "androidx.navigation.navDeepLink")
    private val navClassDeepLinkImportable = Importable("NavDeepLink", "androidx.navigation.NavDeepLink")

    fun argsFromFunctions(
        navArgsType: String,
        additionalArgLine: (String) -> String? = { null }
    ): String {
        if (navArgs.isEmpty()) {
            return ""
        }

        return argsFromNavBackStackEntry(navArgsType, additionalArgLine) + "\n" + argsFromSavedStateHandle(navArgsType, additionalArgLine)
    }

    private fun argsFromNavBackStackEntry(argsType: String, additionalArgLine: (String) -> String?): String {
        val code = StringBuilder()
        code += """
                
           |override fun argsFrom(bundle: ${bundleImportable.getCodePlaceHolder()}?): $argsType {
           |    return ${argsType}(%s2
           |    )
           |}
            """.trimMargin()

        val arguments = StringBuilder()
        navArgs.forEach {
            arguments += "\n\t\t${it.name} = "
            arguments += navArgResolver.resolve(errorLocationPrefix, it)
            arguments += ","
        }

        val additionalLine = additionalArgLine.invoke("bundle")
        if (additionalLine != null) {
            arguments += additionalLine
        }

        return code.toString()
            .replace("%s2", arguments.toString())
            .prependIndent("\t")
    }

    private fun argsFromSavedStateHandle(argsType: String, additionalArgLine: (String) -> String?): String {
        val savedStateHandlePlaceholder = savedStateHandleImportable.getCodePlaceHolder()

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
            arguments += navArgResolver.resolveFromSavedStateHandle(errorLocationPrefix, it)
            arguments += ","
        }

        val additionalLine = additionalArgLine.invoke("savedStateHandle")
        if (additionalLine != null) {
            arguments += additionalLine
        }

        return code.toString()
            .replace("%s2", arguments.toString())
            .prependIndent("\t")
    }

    fun invokeMethodsCode(
        navArgsType: String,
        writeEmptyInvoke: Boolean = true,
        directionRouteSuffix: String = "",
        additionalArgs: Map<String, Importable> = emptyMap()
    ): String {
        if (navArgs.isEmpty()) {

            return if (writeEmptyInvoke) {
                """
                |
                |    public operator fun invoke(): $CORE_DIRECTION = this
                |    
                """.trimMargin()
            } else {
                ""
            }
        }

        val template = """
            |
            |    override fun invoke(navArgs: %s1): $CORE_DIRECTION = with(navArgs) {
            |        invoke(%s2)
            |    }
            |     
            |    public operator fun invoke(
            |%s3
            |    ): $CORE_DIRECTION {
            |        return $CORE_DIRECTION(
            |            route = %s4$directionRouteSuffix
            |        )
            |    }
            |    
            """.trimMargin()

        var route = "\"${constructRoute(true)}\""
            .replace("/", "\" + \n\t\t\t\t\t\"/")
            .replace("?", "\" + \n\t\t\t\t\t\"?")
            .replace("&", "\" + \n\t\t\t\t\t\"&")

        navArgs.forEach {
            route = route.replace("{${it.name}}", "\${${it.stringifyForNavigation()}}")
        }

        val s2Suffix = if (additionalArgs.isNotEmpty()) {
            ", " + additionalArgs.keys.joinToString(",")
        } else {
            ""
        }

        var s3Suffix = ""
        additionalArgs.forEach {
            s3Suffix += "\n\t\t${it.key}: ${it.value.getCodePlaceHolder()}"
        }

        return template
            .replace("%s1", navArgsType)
            .replace("%s2", navArgs.joinToString(", ") { it.name } + s2Suffix)
            .replace("%s3", innerNavArgsParametersCode() + s3Suffix)
            .replace("%s4", route)
    }

    fun innerNavArgsParametersCode(argPrefix: String = "\t\t"): String {
        val args = StringBuilder()

        navArgs.forEachIndexed { i, it ->
            args += "$argPrefix${it.name}: ${it.type.toTypeCode(importableHelper)}${defaultValueForInvokeFunction(it)},"

            if (i != navArgs.lastIndex) {
                args += "\n"
            }
        }

        return args.toString()
    }

    private fun defaultValueForInvokeFunction(it: Parameter): String {
        return if (it.hasDefault) " = ${it.defaultValue?.code}"
        else ""
    }

    fun deepLinksDeclarationCode(
        deepLinks: List<DeepLink>,
        listOfOnly: Boolean = false,
        innerTabsCount: Int = 2,
        fullRoutePlaceholderReplacement: (List<Parameter>) -> String = { constructRoute(true, it) }
    ): String {
        val code = StringBuilder()

        deepLinks.forEachIndexed { i, it ->
            if (i == 0) {
                code += if (listOfOnly) {
                    "listOf(\n${"\t".repeat(innerTabsCount)}"
                } else {
                    "\n\toverride val deepLinks: List<${navClassDeepLinkImportable.getCodePlaceHolder()}> get() = listOf(\n${"\t".repeat(innerTabsCount)}"
                }
            }

            code += "${navDeepLinkImportable.getCodePlaceHolder()} {\n${"\t".repeat(innerTabsCount)}"

            if (it.action.isNotEmpty()) {
                code += "\taction = \"${it.action}\"\n${"\t".repeat(innerTabsCount)}"
            }
            if (it.mimeType.isNotEmpty()) {
                code += "\tmimeType = \"${it.mimeType}\"\n${"\t".repeat(innerTabsCount)}"
            }
            if (it.uriPattern.isNotEmpty()) {
                val uriPattern = if (it.uriPattern.contains(
                        DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER
                    )) {
                    if (it.uriPattern.endsWith(DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER)) {
                        it.uriPattern.replace(DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER, constructRouteForDeepLinkPlaceholder(fullRoutePlaceholderReplacement))
                    } else {
                        throw IllegalDestinationsSetup("$errorLocationPrefix: deep link usage of 'FULL_ROUTE_PLACEHOLDER' must be as a suffix")
                    }
                } else {
                    it.uriPattern
                }
                code += "\turiPattern = \"$uriPattern\"\n${"\t".repeat(innerTabsCount)}"
            }
            code += "}"

            code += if (i != deepLinks.lastIndex) {
                ",\n${"\t".repeat(innerTabsCount)}"
            } else {
                "\n${"\t".repeat(innerTabsCount - 1)})${if (listOfOnly) "" else "\n"}"
            }
        }

        return code.toString()
    }

    private fun constructRouteForDeepLinkPlaceholder(fullRoutePlaceholderReplacement: (List<Parameter>) -> String): String {
        val args = navArgs
            .toMutableList()
            .apply {
                removeAll {
                    val needsCustomSerializer = it.isCustomTypeNavArg() && !it.isEnumTypeOrTypeArg()
                    val hasCustomSerializer = navArgResolver.customNavTypeByType[it.type.value]?.serializer != null
                    if (it.isMandatory && needsCustomSerializer && !hasCustomSerializer) {
                        throw IllegalDestinationsSetup(
                            "$errorLocationPrefix, arg name= '${it.name}': " +
                                    "deep links cannot contain mandatory navigation types of custom type unless you define" +
                                    "a custom serializer with @NavTypeSerializer. " +
                                    "This lets you control how the custom type class is defined in the string route."
                        )
                    }

                    needsCustomSerializer && !it.isMandatory && !hasCustomSerializer
                }
            }

        return fullRoutePlaceholderReplacement(args)
    }

    fun navArgumentsDeclarationCode(suffixAfterListOf: String = ""): String {
        val code = StringBuilder()

        navArgs.forEachIndexed { i, it ->
            if (i == 0) {
                code += "\n\toverride val arguments: List<${navClassArgumentImportable.getCodePlaceHolder()}> get() = listOf(\n\t\t"
            }

            val toNavTypeCode = it.type.toNavTypeCode()
            code += "${navArgumentImportable.getCodePlaceHolder()}(\"${it.name}\") {\n\t\t\t"
            code += "type = $toNavTypeCode\n\t\t"
            if (it.type.isNullable) {
                code += "\tnullable = true\n\t\t"
            }
            code += navArgDefaultCode(it)
            code += "}"

            code += if (i != navArgs.lastIndex) {
                ",\n\t\t"
            } else {
                "\n\t)$suffixAfterListOf\n"
            }
        }

        return code.toString()
    }

    fun constructRouteFieldCode(): String {
        return if (navArgs.isEmpty()) {
            constructRoute(false)
        } else {
            "\"${constructRoute(true)}\""
        }
    }

    fun constructRoute(
        isConcatenatingInString: Boolean,
        args: List<Parameter> = navArgs,
        baseRouteStr: String = "baseRoute"
    ): String {
        val mandatoryArgs = StringBuilder()
        val optionalArgs = StringBuilder()
        args.forEach {
            if (it.isMandatory) {
                mandatoryArgs += "/{${it.name}}"
            } else {
                val leadingSign = if (optionalArgs.isEmpty()) "?" else "&"
                optionalArgs += "$leadingSign${it.name}={${it.name}}"
            }
        }

        val baseRoutePrefix = if (isConcatenatingInString) {
            "\$$baseRouteStr"
        } else {
            baseRouteStr
        }

        return if (args.isEmpty()) baseRoutePrefix
        else "$baseRoutePrefix$mandatoryArgs$optionalArgs"
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

    private fun TypeInfo.toNavTypeCode(): String {
        val coreNavTypeCode = toCoreNavTypeImportableOrNull()
        if (coreNavTypeCode != null) {
            return coreNavTypeCode.getCodePlaceHolder()
        }

        if (isCustomTypeNavArg()) {
            importable.addImport()
            typeArguments.forEach {
                if (it is TypeArgument.Typed) it.type.importable.addImport()
            }
            return navArgResolver.customNavTypeCode(this)
        }

        if (valueClassInnerInfo != null) {
            return valueClassInnerInfo.typeInfo.toNavTypeCode()
        }

        throw IllegalDestinationsSetup("$errorLocationPrefix: Unknown type ${importable.qualifiedName}")
    }

    private fun Importable.getCodePlaceHolder(): String {
        return importableHelper.addAndGetPlaceholder(this)
    }

    private fun Importable.addImport() {
        importableHelper.addAndGetPlaceholder(this)
    }

    private fun Parameter.stringifyForNavigation(): String {
        return type.stringifyForNavigation(name)
    }

    private fun TypeInfo.stringifyForNavigation(
        argumentName: String,
        argumentReference: String = argumentName,
    ): String {
        if (isCustomTypeNavArg()) {
            val codePlaceHolder = navArgResolver.customNavTypeCode(this)

            return "$codePlaceHolder.serializeValue($argumentReference)"
        }

        if (importable.qualifiedName == String::class.qualifiedName) {
            return "${CORE_STRING_NAV_TYPE.getCodePlaceHolder()}.serializeValue(\"$argumentName\", $argumentReference)"
        } else if (value in coreTypes.keys) {
            return "${coreTypes[value]!!.getCodePlaceHolder()}.serializeValue($argumentReference)"
        }

        if (valueClassInnerInfo != null) {
            return valueClassInnerInfo.typeInfo.stringifyForNavigation(
                argumentName = argumentName,
                argumentReference = "$argumentName${if (isNullable) "?." else "."}${valueClassInnerInfo.publicNonNullableField}"
            )
        }

        val isNullable = isNullable || argumentReference.contains("?.")
        val ifNullBeforeToString = if (isNullable) "?" else ""
        val ifNullSuffix = if (isNullable) {
            " ?: \"{${argumentName}}\""
        } else {
            ""
        }

        return "${argumentReference}$ifNullBeforeToString${".toString()"}$ifNullSuffix"
    }
}