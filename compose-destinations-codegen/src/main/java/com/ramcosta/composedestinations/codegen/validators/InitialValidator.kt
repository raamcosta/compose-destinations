package com.ramcosta.composedestinations.codegen.validators

import com.ramcosta.composedestinations.codegen.commons.ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.commons.BOTTOM_SHEET_DEPENDENCY
import com.ramcosta.composedestinations.codegen.commons.COLUMN_SCOPE_SIMPLE_NAME
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
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParams
import com.ramcosta.composedestinations.codegen.model.DestinationResultSenderInfo
import com.ramcosta.composedestinations.codegen.model.DestinationStyleType
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.model.Visibility

class InitialValidator(
    private val codeGenConfig: CodeGenConfig,
    private val isBottomSheetDependencyPresent: Boolean
) {

    fun validate(
        navGraphs: List<RawNavGraphGenParams>,
        destinations: List<DestinationGeneratingParams>,
        submoduleResultSenders: Map<String, DestinationResultSenderInfo>
    ) {
        validateNavGraphs(navGraphs)

        val destinationsByName = lazy { destinations.associateBy { it.name } }
        val navGraphRoutes = navGraphs.map { it.baseRoute }
        val cleanRoutes = mutableListOf<String>()

        destinations.forEach { destination ->
            destination.checkVisibilityToNavGraph()

            destination.checkNavArgTypes()

            destination.validateRoute(cleanRoutes, navGraphRoutes)

            destination.validateReceiverColumnScope()

            destination.validateReceiverAnimatedVisibilityScope()

            destination.warnIgnoredAnnotationArguments()

            destination.validateOpenResultRecipients()

            destination.validateClosedResultRecipients(submoduleResultSenders, destinationsByName)

            cleanRoutes.add(destination.baseRoute)
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
    }

    private fun DestinationGeneratingParams.warnIgnoredAnnotationArguments() {
        if (!codeGenConfig.generateNavGraphs) {

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
        submoduleResultSenders: Map<String, DestinationResultSenderInfo>,
        destinationsByName: Lazy<Map<String, DestinationGeneratingParams>>
    ) {
        val resultRecipientParams = parameters
            .filter { it.type.importable.qualifiedName == RESULT_RECIPIENT_QUALIFIED_NAME }

        val destinationResultOriginForAllResultTypes = mutableSetOf<String>()

        resultRecipientParams.forEach { parameter ->
            Logger.instance.info("validateClosedResultRecipients | checking param $composableName ${parameter.name}")

            val resultType = (parameter.type.typeArguments[1] as? TypeArgument.Typed)?.type
                ?: throw IllegalDestinationsSetup(
                    "ResultRecipient second type argument must be a valid type with no '*' variance."
                )
            validateResultType(resultType)

            val resultOriginQualifiedName = parameter.getFirstArgTypeQualifiedName()

            if (resultOriginQualifiedName != null) {
                // qualified name means that this is a Destination which is already generated
                // so a Destination that comes from a dependency module
                destinationResultOriginForAllResultTypes.add(resultOriginQualifiedName)
                val info = submoduleResultSenders[resultOriginQualifiedName]

                if (info == null || info.resultTypeQualifiedName != resultType.importable.qualifiedName || info.isResultTypeNullable != resultType.isNullable) {
                    throw IllegalDestinationsSetup(
                        "Composable correspondent to '${resultOriginQualifiedName}' must receive a 'ResultBackNavigator<${resultType.toTypeCode()}>'" +
                                " parameter in order to be used as result originator for '${composableName}'"
                    )
                }

            } else {
                // no qualified name means that this might be a Destination which is still not generated
                // so a Destination that we will generate ourselves during this ksp run

                val resultOriginDestinationName = parameter.getFirstArgTypeSimpleName()
                destinationResultOriginForAllResultTypes.add(resultOriginDestinationName)

                val resultOriginDestinationParams =
                    destinationsByName.value[resultOriginDestinationName]
                        ?: throw IllegalDestinationsSetup("Non existent Destination ('$resultOriginDestinationName') as the ResultRecipient's result origin (type aliases are not allowed here) for '$composableName'.")

                resultOriginDestinationParams.parameters.firstOrNull {
                    it.type.importable.qualifiedName == RESULT_BACK_NAVIGATOR_QUALIFIED_NAME &&
                            (it.type.typeArguments.firstOrNull() as? TypeArgument.Typed)?.type == resultType
                }
                    ?: throw IllegalDestinationsSetup(
                        "Composable '${resultOriginDestinationParams.composableName}' must receive a ResultBackNavigator" +
                                " of type '${resultType.toTypeCode()}' in order to be used as result originator for '${composableName}'"
                    )
            }
        }

        if (destinationResultOriginForAllResultTypes.size != resultRecipientParams.size) {
            throw IllegalDestinationsSetup(
                "Composable '${composableName}': " +
                        "has multiple ResultRecipients with the same Destination, only one recipient is allowed for a given destination!"
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
            Logger.instance.info("getFirstArgTypeSimpleName | line error = \n```\n${firstTypeArg.lineStr}\n```")
            return firstTypeArg.lineStr
                .replaceBefore(this.name, "")
                .removePrefix(this.name).also {
                    Logger.instance.info("getFirstArgTypeSimpleName | result of removePrefix ${this.name} = \n```\n$it\n```")
                }
                .replaceBefore("ResultRecipient", "")
                .removePrefix("ResultRecipient").also {
                    Logger.instance.info("getFirstArgTypeSimpleName | result of removePrefix ResultRecipient = \n```\n$it\n```")
                }
                .replaceBefore("<", "")
                .removePrefix("<").also {
                    Logger.instance.info("getFirstArgTypeSimpleName | result of removePrefix < = \n```\n$it\n```")
                }
                .replaceAfter(">", "")
                .removeSuffix(">").also {
                    Logger.instance.info("getFirstArgTypeSimpleName | result of removeSuffix > = \n```\n$it\n```")
                }
                .split(",").also {
                    Logger.instance.info("getFirstArgTypeSimpleName | result of split = \n```\n$it\n```")
                }
                .first()
                .trim().also {
                    Logger.instance.info("getFirstArgTypeSimpleName | Result of trim = \n```\n$it\n```")
                }
        }

        return (firstTypeArg as? TypeArgument.Typed)?.type?.importable?.simpleName
            ?: throw IllegalDestinationsSetup("ResultRecipient first type argument must be a Destination")
    }

    private fun Parameter.getFirstArgTypeQualifiedName(): String? {
        val firstTypeArg = type.typeArguments.first()

        return (firstTypeArg as? TypeArgument.Typed)?.type?.importable?.qualifiedName
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

    private fun DestinationGeneratingParams.checkVisibilityToNavGraph() {
        if (navGraphInfo == null && visibility != Visibility.PUBLIC) {
            throw IllegalDestinationsSetup(
                "$composableName has visibility $visibility but it's using 'ExternalModuleGraph'. In order for it to be included in an external module graph, it has to be public!"
            )
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
