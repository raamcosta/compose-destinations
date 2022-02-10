package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*

class InitialValidator(
    private val codeGenConfig: CodeGenConfig,
    private val logger: Logger,
    private val core: Core
) {

    fun validate(destinations: List<DestinationGeneratingParams>) {
        val destinationsByName = lazy { destinations.associateBy { it.name } }
        val cleanRoutes = mutableListOf<String>()
        val composableNames = mutableListOf<String>()

        destinations.forEach { destination ->
            destination.validateRoute(currentKnownRoutes = cleanRoutes)

            destination.validateComposableName(currentKnownComposableNames = composableNames)

            destination.validateReceiverColumnScope()

            destination.validateReceiverAnimatedVisibilityScope()

            destination.warnIgnoredAnnotationArguments()

            destination.validateResultParams(destinationsByName)

            cleanRoutes.add(destination.cleanRoute)
            composableNames.add(destination.composableName)
        }
    }

    private fun DestinationGeneratingParams.warnIgnoredAnnotationArguments() {
        if (codeGenConfig.mode == CodeGenMode.Destinations
            || (codeGenConfig.mode is CodeGenMode.SingleModule && !codeGenConfig.mode.generateNavGraphs)) {
            if (navGraphRoute != "root") {
                logger.warn(
                    "'${composableName}' composable: a navGraph was set but it will be ignored. " +
                            "Reason: nav graphs generation was disabled by ksp gradle configuration."
                )
            }

            if (isStart) {
                logger.warn(
                    "'${composableName}' composable: destination was set as the start destination but that will be ignored. " +
                            "Reason: nav graphs generation was disabled by ksp gradle configuration."
                )
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
        val resultRecipientParams = parameters.filter { it.type.classType.qualifiedName == RESULT_RECIPIENT_QUALIFIED_NAME }
        val destinationResultOriginForAllResultTypes = mutableSetOf<String>()
        resultRecipientParams.forEach { parameter ->
            val resultOriginDestinationName = parameter.getFirstArgTypeSimpleName()
            val resultType = (parameter.type.genericTypes[1] as? TypedGenericType)?.type
                ?: throw IllegalDestinationsSetup("ResultRecipient second type argument must be a valid type with no '*' variance.")

            validateResultType(resultType)
            destinationResultOriginForAllResultTypes.add(resultOriginDestinationName)

            val resultOriginDestinationParams =
                destinationsByName.value[resultOriginDestinationName]
                    ?: throw IllegalDestinationsSetup("Non existent Destination ('$resultOriginDestinationName') as the ResultRecipient's result origin!")

            resultOriginDestinationParams.parameters.firstOrNull {
                it.type.classType.qualifiedName == RESULT_BACK_NAVIGATOR_QUALIFIED_NAME &&
                        (it.type.genericTypes.firstOrNull() as? TypedGenericType)?.type == resultType
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
            parameters.filter { it.type.classType.qualifiedName == RESULT_BACK_NAVIGATOR_QUALIFIED_NAME }
        if (resultBackNavigatorParams.size > 1) {
            throw IllegalDestinationsSetup(
                "Composable '${composableName}': " +
                        "Destination annotated Composables must have at most one ResultBackNavigator"
            )
        }
    }

    private fun Parameter.getFirstArgTypeSimpleName(): String {
        val firstTypeArg = type.genericTypes.first()

        if (firstTypeArg is ErrorGenericType) {
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

        return (firstTypeArg as? TypedGenericType)?.type?.classType?.simpleName
            ?: throw IllegalDestinationsSetup("ResultRecipient first type argument must be a Destination")
    }

    private fun DestinationGeneratingParams.validateResultType(resultType: Type) {
        if (resultType.genericTypes.isNotEmpty()) {
            throw IllegalDestinationsSetup("Composable $composableName, ${resultType.toTypeCode()}: Result types cannot have type arguments!")
        }

        if (!resultType.isPrimitive() && !resultType.isSerializable && !resultType.isParcelable) {
            throw IllegalDestinationsSetup("Composable $composableName, ${resultType.toTypeCode()}: Result types must be one of: ${primitiveTypes.keys.toMutableList().apply { add("Parcelable"); add("Serializable") }.joinToString(",")}")
        }
    }
}