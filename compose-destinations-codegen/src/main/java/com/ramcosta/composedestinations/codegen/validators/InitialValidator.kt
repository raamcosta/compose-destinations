package com.ramcosta.composedestinations.codegen.validators

import com.ramcosta.composedestinations.codegen.commons.ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.commons.BOTTOM_SHEET_DEPENDENCY
import com.ramcosta.composedestinations.codegen.commons.COLUMN_SCOPE_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.MissingRequiredDependency
import com.ramcosta.composedestinations.codegen.commons.OPEN_RESULT_RECIPIENT_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.RESULT_BACK_NAVIGATOR_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.RESULT_RECIPIENT_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.firstTypeArg
import com.ramcosta.composedestinations.codegen.commons.firstTypeInfoArg
import com.ramcosta.composedestinations.codegen.commons.isCoreType
import com.ramcosta.composedestinations.codegen.commons.isCustomArrayOrArrayListTypeNavArg
import com.ramcosta.composedestinations.codegen.commons.isCustomTypeNavArg
import com.ramcosta.composedestinations.codegen.commons.toTypeCode
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParams
import com.ramcosta.composedestinations.codegen.model.DestinationResultSenderInfo
import com.ramcosta.composedestinations.codegen.model.DestinationStyleType
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.RawNavArgsClass
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.model.Visibility

internal class InitialValidator(
    private val codeGenConfig: CodeGenConfig,
) {

    fun validate(
        navGraphs: List<RawNavGraphGenParams>,
        destinations: List<DestinationGeneratingParams>,
        submoduleResultSenders: Map<String, DestinationResultSenderInfo>
    ): List<CodeGenProcessedDestination> {
        navGraphs.validate()
        destinations.validate(navGraphs, submoduleResultSenders)

        return destinations.map {
            CodeGenProcessedDestination(
                it.getNavArgs(),
                it
            )
        }
    }

    private fun List<DestinationGeneratingParams>.validate(
        navGraphs: List<RawNavGraphGenParams>,
        submoduleResultSenders: Map<String, DestinationResultSenderInfo>
    ) {
        val destinationsByName = lazy { associateBy { it.name } }
        val navGraphRoutes = navGraphs.map { it.baseRoute }
        val cleanRoutes = mutableListOf<String>()

        forEach { destination ->
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

    private fun List<RawNavGraphGenParams>.validate() {
        val navGraphsByRoute: Map<String, List<RawNavGraphGenParams>> = groupBy { it.baseRoute }
        navGraphsByRoute.forEach {
            if (it.value.size > 1) {
                throw IllegalDestinationsSetup(
                    "${it.value.joinToString(",")} have" +
                            " the same final nav graph route: ${it.key}." +
                            "Nav graph routes must be unique!"
                )
            }
        }

        forEach {
            it.navArgs?.validateNavArgs("NavGraph annotation '${it.annotationType.simpleName}': ")
        }
    }

    private fun DestinationGeneratingParams.getNavArgs(): List<Parameter> {
        val navArgsDelegateTypeLocal = destinationNavArgsClass
        return if (navArgsDelegateTypeLocal == null) {
            parameters.filter { it.isNavArg() }
        } else {
            navArgsDelegateTypeLocal.validateNavArgs("Composable '${annotatedName}': ")

            val navArgInFuncParams =
                parameters.firstOrNull { it.isNavArg() && it.type.value.importable != navArgsDelegateTypeLocal.type }
            if (navArgInFuncParams != null) {
                throw IllegalDestinationsSetup(
                    "Composable '${annotatedName}': annotated " +
                            "function cannot define arguments of navigation type if using a '$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT' class. (check argument '${navArgInFuncParams.name})'"
                )
            }

            navArgsDelegateTypeLocal.parameters
        }
    }

    private fun RawNavArgsClass.validateNavArgs(
        errorLocationInfo: String,
    ) {
        val nonNavArg = parameters.firstOrNull { !it.isNavArg() }
        if (nonNavArg != null) {
            if (!nonNavArg.type.isCoreOrCustomNavArgType() &&
                nonNavArg.type.valueClassInnerInfo != null && // is value class
                !nonNavArg.type.isValueClassOfValidInnerNavArgType()
            ) {

                throw IllegalDestinationsSetup(
                    errorLocationInfo +
                            "'$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT' cannot have arguments that are not navigation types. (check argument '${nonNavArg.name}')\n" +
                            "HINT: value classes are only valid navigation arguments if they have a public constructor with a public non nullable field which is itself of a navigation type."
                )
            }

            throw IllegalDestinationsSetup(
                errorLocationInfo +
                        "'$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT' cannot have arguments that are not navigation types. (check argument '${nonNavArg.name}')"
            )
        }
    }

    private fun DestinationGeneratingParams.warnIgnoredAnnotationArguments() {
        if (!codeGenConfig.generateNavGraphs) {

            Logger.instance.warn(
                "'${annotatedName}' composable: is annotated with a `NavGraph` annotation, but it will be ignored." +
                        "Reason: nav graphs generation was disabled by ksp gradle configuration."
            )
        }
    }

    private fun DestinationGeneratingParams.validateReceiverAnimatedVisibilityScope() {
        if (composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME) {
            if (destinationStyleType !is DestinationStyleType.Animated && destinationStyleType !is DestinationStyleType.Default) {
                throw IllegalDestinationsSetup(
                    "'${annotatedName}' composable: " +
                            "Only destinations with a DestinationStyle.Animated or DestinationStyle.Default style may have a $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME receiver!"
                )
            }
        }
    }

    private fun DestinationGeneratingParams.validateReceiverColumnScope() {
        if (composableReceiverSimpleName == COLUMN_SCOPE_SIMPLE_NAME) {
            if (!codeGenConfig.isBottomSheetDependencyPresent) {
                throw MissingRequiredDependency(
                    "'${annotatedName}' composable: " +
                            "You need to include $BOTTOM_SHEET_DEPENDENCY dependency to use a $COLUMN_SCOPE_SIMPLE_NAME receiver!"
                )
            }

            if (destinationStyleType !is DestinationStyleType.BottomSheet) {
                throw IllegalDestinationsSetup(
                    "'${annotatedName}' composable: " +
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
            throw IllegalDestinationsSetup("There is a NavGraph with same base route as destination '$annotatedName'")
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
            Logger.instance.info("validateClosedResultRecipients | checking param $annotatedName ${parameter.name}")

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
                                " parameter in order to be used as result originator for '${annotatedName}'"
                    )
                }

            } else {
                // no qualified name means that this might be a Destination which is still not generated
                // so a Destination that we will generate ourselves during this ksp run

                val resultOriginDestinationName = parameter.getFirstArgTypeSimpleName()
                destinationResultOriginForAllResultTypes.add(resultOriginDestinationName)

                val resultOriginDestinationParams =
                    destinationsByName.value[resultOriginDestinationName]
                        ?: throw IllegalDestinationsSetup("Non existent Destination ('$resultOriginDestinationName') as the ResultRecipient's result origin (type aliases are not allowed here) for '$annotatedName'.")

                resultOriginDestinationParams.parameters.firstOrNull {
                    it.type.importable.qualifiedName == RESULT_BACK_NAVIGATOR_QUALIFIED_NAME &&
                            (it.type.typeArguments.firstOrNull() as? TypeArgument.Typed)?.type == resultType
                }
                    ?: throw IllegalDestinationsSetup(
                        "Composable '${resultOriginDestinationParams.annotatedName}' must receive a ResultBackNavigator" +
                                " of type '${resultType.toTypeCode()}' in order to be used as result originator for '${annotatedName}'"
                    )
            }
        }

        if (destinationResultOriginForAllResultTypes.size != resultRecipientParams.size) {
            throw IllegalDestinationsSetup(
                "Composable '${annotatedName}': " +
                        "has multiple ResultRecipients with the same Destination, only one recipient is allowed for a given destination!"
            )
        }

        val resultBackNavigatorParams =
            parameters.filter { it.type.importable.qualifiedName == RESULT_BACK_NAVIGATOR_QUALIFIED_NAME }
        if (resultBackNavigatorParams.size > 1) {
            throw IllegalDestinationsSetup(
                "Composable '${annotatedName}': " +
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
            Logger.instance.info("getFirstArgTypeSimpleName | line error = \n```\n${firstTypeArg.linesStr}\n```")
            return firstTypeArg.linesStr
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
        if (!resultType.isNavArgType()) {
            throw IllegalDestinationsSetup("Composable $annotatedName, ${resultType.toTypeCode()}: Result types must be of a valid navigation argument type.")
        }
    }

    private fun DestinationGeneratingParams.checkVisibilityToNavGraph() {
        if (navGraphInfo == null && visibility != Visibility.PUBLIC) {
            throw IllegalDestinationsSetup(
                "$annotatedName has visibility $visibility but it's using 'ExternalModuleGraph'. In order for it to be included in an external module graph, it has to be public!"
            )
        }
    }


    private fun Parameter.isNavArg(): Boolean {
        if (isMarkedNavHostParam) {
            if (!type.isNavArgType()) {
                Logger.instance.info(
                    "Parameter ${this.name}: annotation @NavHostParam is redundant since it" +
                            " is not a navigation argument type anyway."
                )
            }
            return false
        }

        return type.isNavArgType()
    }

    private fun TypeInfo.isNavArgType(): Boolean {
        if (isCoreOrCustomNavArgType()) {
            return true
        }

        return isValueClassOfValidInnerNavArgType()
    }

    private fun TypeInfo.isValueClassOfValidInnerNavArgType(): Boolean {
        if (valueClassInnerInfo != null &&
            valueClassInnerInfo.isConstructorPublic &&
            valueClassInnerInfo.publicNonNullableField != null
        ) {
            return valueClassInnerInfo.typeInfo.isCoreOrCustomNavArgType()
        }

        return false
    }

    private fun TypeInfo.isCoreOrCustomNavArgType(): Boolean {
        if (isCoreType()) {
            return true
        }

        if (isCustomTypeNavArg()) {
            return true
        }

        return false
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
}
