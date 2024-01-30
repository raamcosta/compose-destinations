package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.commons.BOTTOM_SHEET_DEPENDENCY
import com.ramcosta.composedestinations.codegen.commons.CORE_ACTIVITY_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_BOTTOM_SHEET_DESTINATION_STYLE
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION_ACTIVITY_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_TYPED_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.MissingRequiredDependency
import com.ramcosta.composedestinations.codegen.commons.experimentalAnimationApiType
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.removeInstancesOf
import com.ramcosta.composedestinations.codegen.commons.setOfPublicStartParticipatingTypes
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.DestinationStyleType
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.codegen.templates.ACTIVITY_DESTINATION_FIELDS
import com.ramcosta.composedestinations.codegen.templates.ARGS_FROM_METHODS
import com.ramcosta.composedestinations.codegen.templates.ARGS_TO_DIRECTION_METHOD
import com.ramcosta.composedestinations.codegen.templates.BASE_ROUTE
import com.ramcosta.composedestinations.codegen.templates.COMPOSED_ROUTE
import com.ramcosta.composedestinations.codegen.templates.CONTENT_FUNCTION_CODE
import com.ramcosta.composedestinations.codegen.templates.DEEP_LINKS
import com.ramcosta.composedestinations.codegen.templates.DESTINATION_NAME
import com.ramcosta.composedestinations.codegen.templates.DESTINATION_STYLE
import com.ramcosta.composedestinations.codegen.templates.DESTINATION_VISIBILITY_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_ARGS_DATA_CLASS
import com.ramcosta.composedestinations.codegen.templates.NAV_ARGUMENTS
import com.ramcosta.composedestinations.codegen.templates.REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.SUPERTYPE
import com.ramcosta.composedestinations.codegen.templates.USER_COMPOSABLE_DESTINATION
import com.ramcosta.composedestinations.codegen.templates.destinationTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.NavArgResolver
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationContentFunctionWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavArgumentBridgeCodeBuilder

val destinationsPackageName = "$codeGenBasePackageName.destinations"

internal class SingleDestinationWriter(
    private val codeGenConfig: CodeGenConfig,
    private val codeGenerator: CodeOutputStreamMaker,
    private val isBottomSheetDependencyPresent: Boolean,
    private val navArgResolver: NavArgResolver,
    private val destination: CodeGenProcessedDestination,
    private val importableHelper: ImportableHelper
) {

    private val navArgs get() = destination.navArgs
    private val navArgumentBridgeCodeBuilder = NavArgumentBridgeCodeBuilder(
        importableHelper,
        navArgResolver,
        navArgs,
        "Composable '${destination.composableName}'"
    )

    init {
        if (destination.isParentStart && destination.navGraphInfo?.isNavHostGraph == true && destination.navArgs.any { it.isMandatory }) {
            throw IllegalDestinationsSetup("\"'${destination.composableName}' composable: Start destinations of NavHostGraphs cannot have mandatory navigation arguments!")
        }

        importableHelper.addAll(destinationTemplate.imports)
        importableHelper.addPriorityQualifiedImport(destination.composableQualifiedName, destination.composableName)
    }

    fun write() = with(destination) {
        codeGenerator.makeFile(
            packageName = destinationsPackageName,
            name = name,
            sourceIds = sourceIds.toTypedArray()
        ).writeSourceFile(
            packageStatement = destinationTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = destinationTemplate.sourceCode
                .replace(DESTINATION_NAME, name)
                .replace(USER_COMPOSABLE_DESTINATION, composableName)
                .replaceSuperclassDestination()
                .addNavArgsDataClass()
                .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, objectWideRequireOptInAnnotationsCode())
                .replace(DESTINATION_VISIBILITY_PLACEHOLDER, getDestinationVisibilityModifier())
                .replace(BASE_ROUTE, destination.baseRoute)
                .replace(COMPOSED_ROUTE, navArgumentBridgeCodeBuilder.constructRouteFieldCode())
                .replace(NAV_ARGUMENTS, navArgumentBridgeCodeBuilder.navArgumentsDeclarationCode())
                .replace(
                    DEEP_LINKS,
                    navArgumentBridgeCodeBuilder.deepLinksDeclarationCode(destination.deepLinks)
                )
                .replace(DESTINATION_STYLE, destinationStyle())
                .replace(CONTENT_FUNCTION_CODE, contentFunctionCode())
                .replace(ARGS_TO_DIRECTION_METHOD, navArgumentBridgeCodeBuilder.invokeMethodsCode(navArgsDataClassName()))
                .replace(
                    ARGS_FROM_METHODS,
                    navArgumentBridgeCodeBuilder.argsFromFunctions(navArgsDataClassName())
                )
                .replace(ACTIVITY_DESTINATION_FIELDS, activityDestinationFields())
        )
    }

    private fun getDestinationVisibilityModifier(): String {
        return destination.visibility.name.lowercase()
    }

    private fun String.replaceSuperclassDestination(): String {
        if (destination.destinationStyleType is DestinationStyleType.Activity) {
            return replace(
                SUPERTYPE, if (navArgs.isEmpty()) {
                    CORE_DIRECTION_ACTIVITY_DESTINATION_SPEC.simpleName
                } else {
                    "${CORE_ACTIVITY_DESTINATION_SPEC.simpleName}<${destination.destinationNavArgsClass!!.type.getCodePlaceHolder()}>"
                }
            )
        }

        if (navArgs.isEmpty()) {
            return replace(SUPERTYPE, CORE_DIRECTION_DESTINATION_SPEC)
        }

        val superType = if (destination.destinationNavArgsClass != null) {
            "${CORE_TYPED_DESTINATION_SPEC}<${destination.destinationNavArgsClass.type.getCodePlaceHolder()}>"
        } else {
            "${CORE_TYPED_DESTINATION_SPEC}<${destination.name}NavArgs>"
        }

        return replace(SUPERTYPE, superType)
    }

    private fun String.addNavArgsDataClass(): String {
        if (navArgs.isEmpty() || destination.destinationNavArgsClass != null) {
            return removeInstancesOf(NAV_ARGS_DATA_CLASS)
        }

        val code = StringBuilder()
        code += "${getGenNavArgsClassVisibility()} data class ${destination.name}NavArgs(\n"
        code += "${navArgumentBridgeCodeBuilder.innerNavArgsParametersCode("\tval ")}\n"
        code += ")\n\n"

        return replace(NAV_ARGS_DATA_CLASS, code.toString())
    }

    private fun getGenNavArgsClassVisibility(): String {
        return when (destination.visibility) {
            Visibility.PUBLIC -> "public"
            Visibility.INTERNAL -> if (destination.destinationImportable in setOfPublicStartParticipatingTypes) {
                "public"
            } else {
                "internal"
            }
            Visibility.PRIVATE -> error("unexpected visibility ${destination.visibility}")
        }
    }

    private fun objectWideRequireOptInAnnotationsCode(): String {
        val code = StringBuilder()
        val optInByAnnotation = destination.optInAnnotations

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

    private fun activityDestinationFields(): String = with(destination) {
        if (activityDestinationParams == null) {
            return ""
        }

        val uriImportable = Importable(
            "Uri",
            "android.net.Uri"
        )

        val activityImportable = Importable(
            "Activity",
            "android.app.Activity"
        )

        val activityClassImportable = Importable(
            composableName,
            composableQualifiedName
        )

        return """override val targetPackage: String? = @targetPackage@ 
        |    
        |    override val action: String? = @action@ 
        |    
        |    override val data: ${uriImportable.getCodePlaceHolder()}? = @data@ 
        |    
        |    override val dataPattern: String? = @dataPattern@ 
        |    
        |    override val activityClass: Class<out ${activityImportable.getCodePlaceHolder()}>? = @activityClass@::class.java
        |    
        """.trimMargin()
            .replace("@targetPackage@", activityDestinationParams.targetPackage?.let { "\"${it}\"" } ?: "null")
            .replace("@action@", activityDestinationParams.action?.let { "\"${it}\"" } ?: "null")
            .replace("@data@", activityDestinationParams.dataUri?.let { "${uriImportable.getCodePlaceHolder()}.parse(\"${it}\")" } ?: "null")
            .replace("@dataPattern@", activityDestinationParams.dataPattern?.let { "\"${it}\"" } ?: "null")
            .replace("@activityClass@", activityClassImportable.getCodePlaceHolder())
    }

    private fun navArgsDataClassName(): String =
        destination.navArgsDataClassImportable?.getCodePlaceHolder() ?: "Unit"

    private fun contentFunctionCode(): String {
        if (destination.destinationStyleType is DestinationStyleType.Activity) {
            return ""
        }

        return """
    @Composable
    override fun DestinationScope<${navArgsDataClassName()}>.Content() {
%s1
    }
        """.trimIndent()
            .replace(
                "%s1", DestinationContentFunctionWriter(
                    destination,
                    navArgs,
                    importableHelper
                ).write()
            )
    }

    private fun destinationStyle(): String {
        return when (destination.destinationStyleType) {
            is DestinationStyleType.Activity,
            is DestinationStyleType.Default -> ""

            is DestinationStyleType.BottomSheet -> destinationStyleBottomSheet()

            is DestinationStyleType.Animated -> destinationStyleAnimated(destination.destinationStyleType)

            is DestinationStyleType.Dialog -> destinationStyleDialog(destination.destinationStyleType)
        }
    }

    private fun destinationStyleDialog(destinationStyleType: DestinationStyleType.Dialog): String {
        return "\n\toverride val style: DestinationStyle = ${destinationStyleType.code(importableHelper)}\n"
    }

    private fun destinationStyleAnimated(destinationStyleType: DestinationStyleType.Animated): String {
        experimentalAnimationApiType.addImport()

        if (destination.composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME) {
            Importable(
                ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME,
                ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME
            ).addImport()
        }

        return "\n\toverride val style: DestinationStyle = ${destinationStyleType.code(importableHelper)}\n"
    }

    private fun destinationStyleBottomSheet(): String {
        if (!isBottomSheetDependencyPresent) {
            throw MissingRequiredDependency("You need to include '$BOTTOM_SHEET_DEPENDENCY' to use $CORE_BOTTOM_SHEET_DESTINATION_STYLE!")
        }

        return "\n\toverride val style: DestinationStyle = ${DestinationStyleType.BottomSheet.code(importableHelper)}\n"
    }

    private fun Importable.getCodePlaceHolder(): String {
        return importableHelper.addAndGetPlaceholder(this)
    }

    private fun Importable.addImport() {
        importableHelper.addAndGetPlaceholder(this)
    }
}
