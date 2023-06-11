package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenActivityDestination
import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.codeGenDestination
import com.ramcosta.composedestinations.codegen.codeGenNoArgsActivityDestination
import com.ramcosta.composedestinations.codegen.codeGenNoArgsDestination
import com.ramcosta.composedestinations.codegen.commons.ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.commons.BOTTOM_SHEET_DEPENDENCY
import com.ramcosta.composedestinations.codegen.commons.CORE_BOTTOM_SHEET_DESTINATION_STYLE
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.MissingRequiredDependency
import com.ramcosta.composedestinations.codegen.commons.experimentalAnimationApiType
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.recursiveRequireOptInAnnotations
import com.ramcosta.composedestinations.codegen.commons.removeInstancesOf
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParamsWithNavArgs
import com.ramcosta.composedestinations.codegen.model.DestinationStyleType
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.RawNavArgsClass
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
import com.ramcosta.composedestinations.codegen.templates.destinationTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.NavArgResolver
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationContentFunctionWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavArgumentBridgeCodeBuilder

val destinationsPackageName = "$codeGenBasePackageName.destinations"

class SingleDestinationWriter(
    private val codeGenConfig: CodeGenConfig,
    private val codeGenerator: CodeOutputStreamMaker,
    private val isBottomSheetDependencyPresent: Boolean,
    private val navArgResolver: NavArgResolver,
    private val destination: DestinationGeneratingParamsWithNavArgs,
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
        if (destination.navGraphInfo.start && destination.navGraphInfo.isNavHostGraph && destination.navArgs.any { it.isMandatory }) {
            throw IllegalDestinationsSetup("\"'${destination.composableName}' composable: Start destinations of NavHostGraphs cannot have mandatory navigation arguments!")
        }

        importableHelper.addAll(destinationTemplate.imports)
        importableHelper.addPriorityQualifiedImport(destination.composableQualifiedName, destination.composableName)
    }

    fun write(): GeneratedDestination = with(destination) {
        codeGenerator.makeFile(
            packageName = destinationsPackageName,
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
                .replace(DESTINATION_VISIBILITY_PLACEHOLDER, getDestinationVisibilityModifier())
                .replace(BASE_ROUTE, destination.baseRoute)
                .replace(COMPOSED_ROUTE, navArgumentBridgeCodeBuilder.constructRouteFieldCode())
                .replace(NAV_ARGUMENTS, navArgumentBridgeCodeBuilder.navArgumentsDeclarationCode())
                .replace(
                    DEEP_LINKS,
                    navArgumentBridgeCodeBuilder.deepLinksDeclarationCode(destination.deepLinks) {
                        navArgumentBridgeCodeBuilder.constructRoute(true, it)
                    }
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

        return GeneratedDestination(
            sourceIds = sourceIds,
            qualifiedName = "$destinationsPackageName.$name",
            simpleName = name,
            navArgsClass = navArgsDataClassImportable()?.let {
                RawNavArgsClass(
                    parameters = navArgs,
                    type =
                    if (destinationNavArgsClass == null) {
                        it.copy(
                            simpleName = "NavArgs",
                            qualifiedName = "$destinationsPackageName.$name.NavArgs"
                        )
                    } else {
                        it
                    }
                )
            },
            hasMandatoryNavArgs = navArgs.any { it.isMandatory },
            navGraphInfo = navGraphInfo,
            requireOptInAnnotationTypes = gatherOptInAnnotations()
                .filter { !it.isOptedIn }
                .map { it.importable }
                .toList(),
        )
    }

    private fun getDestinationVisibilityModifier(): String {
        return if (codeGenConfig.useComposableVisibility && destination.visibility == Visibility.INTERNAL) "internal"
        else "public"
    }

    private fun String.replaceSuperclassDestination(): String {
        if (destination.destinationStyleType is DestinationStyleType.Activity) {
            return replace(
                SUPERTYPE, if (navArgs.isEmpty()) {
                    codeGenNoArgsActivityDestination
                } else {
                    "$codeGenActivityDestination<${destination.destinationNavArgsClass!!.type.getCodePlaceHolder()}>"
                }
            )
        }

        if (navArgs.isEmpty()) {
            return replace(SUPERTYPE, codeGenNoArgsDestination)
        }

        val superType = if (destination.destinationNavArgsClass != null) {
            "${codeGenDestination}<${destination.destinationNavArgsClass.type.getCodePlaceHolder()}>"
        } else {
            "${codeGenDestination}<${destination.name}.NavArgs>"
        }

        return replace(SUPERTYPE, superType)
    }

    private fun String.addNavArgsDataClass(): String {
        if (navArgs.isEmpty() || destination.destinationNavArgsClass != null) {
            return removeInstancesOf(NAV_ARGS_DATA_CLASS)
        }

        val code = StringBuilder()
        code += "\n"
        code += "\tpublic data class NavArgs(\n"
        code += "${navArgumentBridgeCodeBuilder.innerNavArgsParametersCode(true)}\n"
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

    private fun navArgsDataClassImportable(): Importable? = with(destination) {
        return destinationNavArgsClass?.type
            ?: if (navArgs.isEmpty()) {
                null
            } else {
                Importable(
                    "NavArgs",
                    "$destinationsPackageName.${destination.name}.NavArgs"
                )
            }
    }

    private fun navArgsDataClassName(): String =
        navArgsDataClassImportable()?.getCodePlaceHolder() ?: "Unit"

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
        return "\n\toverride val style: DestinationStyle = ${destinationStyleType.importable.getCodePlaceHolder()}\n"
    }

    private fun destinationStyleAnimated(destinationStyleType: DestinationStyleType.Animated): String {
        experimentalAnimationApiType.addImport()

        if (destination.composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME) {
            Importable(
                ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME,
                ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME
            ).addImport()
        }

        return "\n\toverride val style: DestinationStyle = ${destinationStyleType.importable.getCodePlaceHolder()}\n"
    }

    private fun destinationStyleBottomSheet(): String {
        if (!isBottomSheetDependencyPresent) {
            throw MissingRequiredDependency("You need to include '$BOTTOM_SHEET_DEPENDENCY' to use $CORE_BOTTOM_SHEET_DESTINATION_STYLE!")
        }

        val bottomSheetImportable = Importable(
            CORE_BOTTOM_SHEET_DESTINATION_STYLE,
            "$CORE_PACKAGE_NAME.bottomsheet.spec.$CORE_BOTTOM_SHEET_DESTINATION_STYLE",
        )

        return "\n\toverride val style: DestinationStyle = ${bottomSheetImportable.getCodePlaceHolder()}\n"
    }

    private class OptInAnnotation(
        val importable: Importable,
        val isOptedIn: Boolean,
    )

    private fun Importable.getCodePlaceHolder(): String {
        return importableHelper.addAndGetPlaceholder(this)
    }

    private fun Importable.addImport() {
        importableHelper.addAndGetPlaceholder(this)
    }
}
