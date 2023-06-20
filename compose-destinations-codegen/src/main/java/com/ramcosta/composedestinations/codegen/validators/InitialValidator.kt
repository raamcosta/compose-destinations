package com.ramcosta.composedestinations.codegen.validators

import com.ramcosta.composedestinations.codegen.commons.ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.commons.BOTTOM_SHEET_DEPENDENCY
import com.ramcosta.composedestinations.codegen.commons.COLUMN_SCOPE_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.commons.DOCS_WEBSITE_MULTI_MODULE_CONFIGS
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.OPEN_RESULT_RECIPIENT_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.RESULT_BACK_NAVIGATOR_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.RESULT_RECIPIENT_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.firstTypeArg
import com.ramcosta.composedestinations.codegen.commons.firstTypeInfoArg
import com.ramcosta.composedestinations.codegen.commons.isCustomArrayOrArrayListTypeNavArg
import com.ramcosta.composedestinations.codegen.commons.toTypeCode
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenMode
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParams
import com.ramcosta.composedestinations.codegen.model.DestinationStyleType
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeInfo

class InitialValidator(
    private val codeGenConfig: CodeGenConfig,
    private val isBottomSheetDependencyPresent: Boolean
) {

    fun validate(
        navGraphs: List<RawNavGraphGenParams>,
        destinations: List<DestinationGeneratingParams>
    ) {
        validateNavGraphs(navGraphs)

        val destinationsByName = lazy { destinations.associateBy { it.name } }
        val navGraphRoutes = navGraphs.map { it.baseRoute }
        val cleanRoutes = mutableListOf<String>()
        val composableNames = mutableListOf<String>()

        destinations.forEach { destination ->
            destination.checkNavArgTypes()

            destination.validateRoute(cleanRoutes, navGraphRoutes)

            destination.validateComposableName(composableNames)

            destination.validateReceiverColumnScope()

            destination.validateReceiverAnimatedVisibilityScope()

            destination.warnIgnoredAnnotationArguments()

            destination.validateOpenResultRecipients()

            destination.validateClosedResultRecipients(destinationsByName)

            cleanRoutes.add(destination.baseRoute)
            composableNames.add(destination.composableName)
        }
    }

    private fun validateNavGraphs(navGraphs: List<RawNavGraphGenParams>) {
        val navGraphsByRoute: Map<String, List<RawNavGraphGenParams>> = navGraphs.groupBy { it.baseRoute }
        navGraphsByRoute.forEach {
            if (it.value.size > 1) {
                throw IllegalDestinationsSetup(
                    "${it.value.joinToString(",")} have" +
                            " the same final nav graph route: ${it.key}." +
                            "Nav graph routes must be unique!"
                )
            }
        }

        val defaultNavGraphs = navGraphs.filter { it.default }
        if (defaultNavGraphs.size > 1) {
            throw IllegalDestinationsSetup(
                "${defaultNavGraphs.joinToString(",")} are" +
                        " marked as the default nav graph. Only one nav graph can be the default one!"
            )
        }

        val nestedGraphsWithoutParent = navGraphs.filter { !it.isNavHostGraph && it.parent == null }
        if (nestedGraphsWithoutParent.isNotEmpty() && codeGenConfig.mode is CodeGenMode.SingleModule) {
            throw IllegalDestinationsSetup(
                "[${nestedGraphsWithoutParent.joinToString(",") { "'${it.type.preferredSimpleName}'" }}] are " +
                        "not @NavHostGraph but do not define a parent graph. " +
                        "If this graph is meant to be used on another module, use a different multi module mode: " +
                        DOCS_WEBSITE_MULTI_MODULE_CONFIGS
            )
        }
    }

    private fun DestinationGeneratingParams.warnIgnoredAnnotationArguments() {
        if (codeGenConfig.mode == CodeGenMode.Destinations
            || (codeGenConfig.mode is CodeGenMode.SingleModule && !codeGenConfig.mode.generateNavGraphs)) {

            Logger.instance.warn(
                "'${composableName}' composable: is annotated with a `NavGraph` annotation, but it will be ignored." +
                        "Reason: nav graphs generation was disabled by ksp gradle configuration."
            )
        }
    }

    private fun DestinationGeneratingParams.validateReceiverAnimatedVisibilityScope() {
        if (composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME) {
            if (destinationStyleType !is DestinationStyleType.Animated && destinationStyleType !is DestinationStyleType.Default) {
                throw IllegalDestinationsSetup(
                    "'${composableName}' composable: " +
                            "Only destinations with a DestinationStyle.Animated or DestinationStyle.Default style may have a $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME receiver!"
                )
            }
        }
    }

    private fun DestinationGeneratingParams.validateReceiverColumnScope() {
        if (composableReceiverSimpleName == COLUMN_SCOPE_SIMPLE_NAME) {
            if (!isBottomSheetDependencyPresent) {
                throw IllegalDestinationsSetup(
                    "'${composableName}' composable: " +
                            "You need to include $BOTTOM_SHEET_DEPENDENCY dependency to use a $COLUMN_SCOPE_SIMPLE_NAME receiver!"
                )
            }

            if (destinationStyleType !is DestinationStyleType.BottomSheet) {
                throw IllegalDestinationsSetup(
                    "'${composableName}' composable: " +
                            "Only destinations with a DestinationStyleBottomSheet style may have a $COLUMN_SCOPE_SIMPLE_NAME receiver!"
                )
            }
        }
    }

    private fun DestinationGeneratingParams.validateComposableName(
        currentKnownComposableNames: List<String>,
    ) {
        if (currentKnownComposableNames.contains(composableName)) {
            throw IllegalDestinationsSetup("Destination composable names must be unique: found multiple named '${composableName}'")
        }
    }

    private fun DestinationGeneratingParams.validateRoute(
        currentKnownRoutes: List<String>,
        navGraphRoutes: List<String>
    ) {
        if (currentKnownRoutes.contains(baseRoute)) {
            throw IllegalDestinationsSetup("Multiple Destinations with '${baseRoute}' as their route name")
        }

        if (navGraphRoutes.contains(baseRoute)) {
            throw IllegalDestinationsSetup("There is a NavGraph with same base route as destination '$composableName'")
        }
    }

    private fun DestinationGeneratingParams.validateClosedResultRecipients(
        destinationsByName: Lazy<Map<String, DestinationGeneratingParams>>
    ) {
        val resultRecipientParams = parameters
            .filter { it.type.importable.qualifiedName == RESULT_RECIPIENT_QUALIFIED_NAME }

        val destinationResultOriginForAllResultTypes = mutableSetOf<String>()

        resultRecipientParams.forEach { parameter ->
            val resultOriginDestinationName = parameter.getFirstArgTypeSimpleName()
            val resultType = (parameter.type.typeArguments[1] as? TypeArgument.Typed)?.type
                ?: throw IllegalDestinationsSetup(
                    "ResultRecipient second type argument must be a valid type with no '*' variance."
                )

            validateResultType(resultType)
            destinationResultOriginForAllResultTypes.add(resultOriginDestinationName)

            val resultOriginDestinationParams =
                destinationsByName.value[resultOriginDestinationName]
                    ?: throw IllegalDestinationsSetup("Non existent Destination ('$resultOriginDestinationName') as the ResultRecipient's result origin!")

            resultOriginDestinationParams.parameters.firstOrNull {
                it.type.importable.qualifiedName == RESULT_BACK_NAVIGATOR_QUALIFIED_NAME &&
                        (it.type.typeArguments.firstOrNull() as? TypeArgument.Typed)?.type == resultType
            }
                ?: throw IllegalDestinationsSetup(
                    "Composable '${resultOriginDestinationParams.composableName}' must receive a ResultBackNavigator" +
                            " of type '${resultType.toTypeCode()}' in order to be used as result originator for '${composableName}'"
                )
        }

        if (destinationResultOriginForAllResultTypes.size != resultRecipientParams.size) {
            throw IllegalDestinationsSetup(
                "Composable '${composableName}': " +
                        "has multiple ResultRecipients with the same Destination, only one recipient is allowed for a destination!"
            )
        }

        val resultBackNavigatorParams =
            parameters.filter { it.type.importable.qualifiedName == RESULT_BACK_NAVIGATOR_QUALIFIED_NAME }
        if (resultBackNavigatorParams.size > 1) {
            throw IllegalDestinationsSetup(
                "Composable '${composableName}': " +
                        "Destination annotated Composables must have at most one ResultBackNavigator"
            )
        }
    }

    private fun DestinationGeneratingParams.validateOpenResultRecipients() {
        val openResultRecipientParams = parameters
            .filter { it.type.importable.qualifiedName == OPEN_RESULT_RECIPIENT_QUALIFIED_NAME }

        openResultRecipientParams.forEach { parameter ->
            val resultType =
                (parameter.type.typeArguments.firstOrNull() as? TypeArgument.Typed?)?.type
                    ?: throw IllegalDestinationsSetup(
                        "OpenResultRecipient type argument must be a valid type with no type arguments."
                    )

            validateResultType(resultType)
        }
    }

    private fun Parameter.getFirstArgTypeSimpleName(): String {
        val firstTypeArg = type.typeArguments.first()

        if (firstTypeArg is TypeArgument.Error) {
            // Since the Destination is not yet generated, we are expecting this to happen
            return firstTypeArg.lineStr
                .replaceBefore("<", "")
                .removePrefix("<")
                .replaceAfter(">", "")
                .removeSuffix(">")
                .split(",")
                .first()
                .trim()
        }

        return (firstTypeArg as? TypeArgument.Typed)?.type?.importable?.simpleName
            ?: throw IllegalDestinationsSetup("ResultRecipient first type argument must be a Destination")
    }

    private fun DestinationGeneratingParams.validateResultType(resultType: TypeInfo) {
        if (resultType.typeArguments.isNotEmpty()) {
            throw IllegalDestinationsSetup("Composable $composableName, ${resultType.toTypeCode()}: Result types cannot have type arguments!")
        }

        val primitives = listOf(
            String::class.qualifiedName,
            Long::class.qualifiedName,
            Boolean::class.qualifiedName,
            Float::class.qualifiedName,
            Int::class.qualifiedName
        )
        if (resultType.importable.qualifiedName !in primitives && !resultType.isSerializable && !resultType.isParcelable) {
            throw IllegalDestinationsSetup("Composable $composableName, ${resultType.toTypeCode()}: " +
                    "Result types must be one of: ${listOf("String", "Long", "Boolean", "Float", "Int", "Parcelable", "java.io.Serializable").joinToString(", ")}")
        }
    }
}

private fun DestinationGeneratingParams.checkNavArgTypes() {
    val navArgsDelegate = destinationNavArgsClass
    if (navArgsDelegate != null) {
        navArgsDelegate.parameters.validateArrayTypeArgs()
    } else {
        parameters.validateArrayTypeArgs()
    }
}

private fun List<Parameter>.validateArrayTypeArgs() {
    val typesOfPrimitiveArrays = mapOf(
        Float::class.qualifiedName to FloatArray::class,
        Int::class.qualifiedName to IntArray::class,
        Boolean::class.qualifiedName to BooleanArray::class,
        Long::class.qualifiedName to LongArray::class,
    )

    forEach { param ->
        val type = param.type
        if (type.isCustomArrayOrArrayListTypeNavArg()) {
            if (type.value.firstTypeInfoArg.isNullable) {
                val typeArg = type.value.typeArguments.first() as TypeArgument.Typed
                val recommendedType = type.copy(
                    value = type.value.copy(
                        typeArguments = listOf(typeArg.copy(type = typeArg.type.copy(isNullable = false)))
                    )
                )
                throw IllegalDestinationsSetup(
                    "'${param.name}: ${type.toTypeCode()}': " +
                            "Arrays / ArrayLists of nullable type arguments are not supported! Maybe " +
                            "'${recommendedType.toTypeCode()}' can be used instead?"
                )
            }

            val qualifiedName = type.value.firstTypeArg.importable.qualifiedName
            if (qualifiedName in typesOfPrimitiveArrays.keys) {
                throw IllegalDestinationsSetup(
                    "'${param.name}: ${type.toTypeCode()}': " +
                            "${type.toTypeCode()} is not allowed, use '${typesOfPrimitiveArrays[qualifiedName]!!.simpleName}' instead."
                )
            }
        }
    }
}
