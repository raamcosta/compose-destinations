package com.ramcosta.composedestinations.codegen.validators

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*

class InitialValidator(
    private val codeGenConfig: CodeGenConfig,
    private val logger: Logger,
    private val core: Core
) {

    fun validate(
        navGraphs: List<RawNavGraphGenParams>,
        destinations: List<DestinationGeneratingParams>
    ) {
        validateNavGraphs(navGraphs)

        val destinationsByName = lazy { destinations.associateBy { it.name } }
        val cleanRoutes = mutableListOf<String>()
        val composableNames = mutableListOf<String>()

        destinations.forEach { destination ->
            destination.checkNavArgTypes()

            destination.checkLegacyNavGraphInfo()

            destination.validateRoute(currentKnownRoutes = cleanRoutes)

            destination.validateComposableName(currentKnownComposableNames = composableNames)

            destination.validateReceiverColumnScope()

            destination.validateReceiverAnimatedVisibilityScope()

            destination.warnIgnoredAnnotationArguments()

            destination.validateResultParams(destinationsByName)

            cleanRoutes.add(destination.cleanRoute)
            composableNames.add(destination.composableName)
        }

        if (destinations.any { it.navGraphInfo is NavGraphInfo.AnnotatedSource } &&
                destinations.any { it.navGraphInfo is NavGraphInfo.Legacy && !it.navGraphInfo.isDefault  }) {
            // User is using both ways to set navgraphs, fail the build
            throw IllegalDestinationsSetup("Cannot use both the deprecated way to set navgraphs and the new one.\n" +
                    "Please migrate all your Destinations to use the new @NavGraph annotations instead!")
        }
    }

    private fun validateNavGraphs(navGraphs: List<RawNavGraphGenParams>) {
        val navGraphsByRoute: Map<String, List<RawNavGraphGenParams>> = navGraphs.groupBy { it.route }
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
    }

    private fun DestinationGeneratingParams.warnIgnoredAnnotationArguments() {
        if (codeGenConfig.mode == CodeGenMode.Destinations
            || (codeGenConfig.mode is CodeGenMode.SingleModule && !codeGenConfig.mode.generateNavGraphs)) {

            when (val info = navGraphInfo) {
                is NavGraphInfo.Legacy -> {
                    if (info.navGraphRoute != "root") {
                        logger.warn(
                            "'${composableName}' composable: a navGraph was set but it will be ignored. " +
                                    "Reason: nav graphs generation was disabled by ksp gradle configuration."
                        )
                    }

                    if (info.start) {
                        logger.warn(
                            "'${composableName}' composable: destination was set as the start destination but that will be ignored. " +
                                    "Reason: nav graphs generation was disabled by ksp gradle configuration."
                        )
                    }
                }

                is NavGraphInfo.AnnotatedSource -> {
                    logger.warn(
                        "'${composableName}' composable: is annotated with a `NavGraph` annotation, but it will be ignored." +
                                "Reason: nav graphs generation was disabled by ksp gradle configuration."
                    )
                }
            }
        }
    }

    private fun DestinationGeneratingParams.validateReceiverAnimatedVisibilityScope() {
        if (composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME) {
            if (core != Core.ANIMATIONS) {
                throw IllegalDestinationsSetup(
                    "'${composableName}' composable: " +
                            "You need to include $CORE_ANIMATIONS_DEPENDENCY dependency to use a $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME receiver!"
                )
            }

            if (destinationStyleType is DestinationStyleType.Dialog || destinationStyleType is DestinationStyleType.BottomSheet) {
                throw IllegalDestinationsSetup(
                    "'${composableName}' composable: " +
                            "Only destinations with a DestinationStyle.Animated or DestinationStyle.Default style may have a $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME receiver!"
                )
            }
        }
    }

    private fun DestinationGeneratingParams.validateReceiverColumnScope() {
        if (composableReceiverSimpleName == COLUMN_SCOPE_SIMPLE_NAME) {
            if (core != Core.ANIMATIONS) {
                throw IllegalDestinationsSetup(
                    "'${composableName}' composable: " +
                            "You need to include $CORE_ANIMATIONS_DEPENDENCY dependency to use a $COLUMN_SCOPE_SIMPLE_NAME receiver!"
                )
            }

            if (destinationStyleType !is DestinationStyleType.BottomSheet) {
                throw IllegalDestinationsSetup(
                    "'${composableName}' composable: " +
                            "Only destinations with a DestinationStyle.BottomSheet style may have a $COLUMN_SCOPE_SIMPLE_NAME receiver!"
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
    ) {
        if (currentKnownRoutes.contains(cleanRoute)) {
            throw IllegalDestinationsSetup("Multiple Destinations with '${cleanRoute}' as their route name")
        }
    }

    private fun DestinationGeneratingParams.validateResultParams(
        destinationsByName: Lazy<Map<String, DestinationGeneratingParams>>
    ) {
        val resultRecipientParams = parameters.filter { it.type.importable.qualifiedName == RESULT_RECIPIENT_QUALIFIED_NAME }
        val destinationResultOriginForAllResultTypes = mutableSetOf<String>()
        resultRecipientParams.forEach { parameter ->
            val resultOriginDestinationName = parameter.getFirstArgTypeSimpleName()
            val resultType = (parameter.type.typeArguments[1] as? TypeArgument.Typed)?.type
                ?: throw IllegalDestinationsSetup("ResultRecipient second type argument must be a valid type with no '*' variance.")

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
                    "Result types must be one of: ${listOf("String", "Long", "Boolean", "Float", "Int", "Parcelable", "Serializable").joinToString(", ")}")
        }
    }

    private fun DestinationGeneratingParams.checkLegacyNavGraphInfo() {
        if (navGraphInfo is NavGraphInfo.Legacy && !navGraphInfo.isDefault) {
            logger.warn("Composable $composableName: Usage of `start` and `navGraph` parameters of @Destination is deprecated.")
        }
    }
}

private fun DestinationGeneratingParams.checkNavArgTypes() {
    val navArgsDelegate = navArgsDelegateType
    if (navArgsDelegate != null) {
        navArgsDelegate.navArgs.validateArrayTypeArgNullability()
    } else {
        parameters.validateArrayTypeArgNullability()
    }
}

private fun List<Parameter>.validateArrayTypeArgNullability() {
    forEach { param ->
        val type = param.type
        if (type.isCustomArrayOrArrayListTypeNavArg() && type.value.firstTypeInfoArg.isNullable) {
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
    }
}
