package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_ALIAS_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_DESTINATION_ANIMATION_STYLE
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION_NAV_HOST_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_NAV_HOST_ANIMATED_DESTINATION_STYLE
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.CORE_TYPED_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_TYPED_NAV_HOST_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.bundleImportable
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.savedStateHandleImportable
import com.ramcosta.composedestinations.codegen.commons.setOfPublicStartParticipatingTypes
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.commons.startingRouteInfo
import com.ramcosta.composedestinations.codegen.commons.toTypeCode
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.ExternalRoute
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.NavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.codegen.templates.INNER_IMPORTED_ROUTES
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_ARGS_FROM
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_ARGUMENTS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_DEEP_LINKS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_DEFAULT_TRANSITIONS
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_DEFAULT_TRANSITIONS_TYPE
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_DESTINATIONS
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_GEN_NAV_ARGS
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_INVOKE_FUNCTION
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_KDOC
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_NAME_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_START_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_START_TYPED_ROUTE_TYPE
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_TYPE
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_VISIBILITY_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NESTED_NAV_GRAPHS
import com.ramcosta.composedestinations.codegen.templates.REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.USER_NAV_GRAPH_ANNOTATION
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.templates.moduleNavGraphTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.NavArgResolver
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

val navGraphsPackageName = "$codeGenBasePackageName.navgraphs"

internal class SingleNavGraphWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val importableHelper: ImportableHelper,
    private val navGraph: RawNavGraphTree,
    private val navArgResolver: NavArgResolver
) {
    private val navGraphType = CORE_TYPED_NAV_GRAPH_SPEC
    private val typedNavHostNavGraphType = CORE_TYPED_NAV_HOST_GRAPH_SPEC
    private val directionNavHostNavGraphType = CORE_DIRECTION_NAV_HOST_GRAPH_SPEC
    private val directionNavGraphType = CORE_DIRECTION_NAV_GRAPH_SPEC

    private val navArgumentBridgeCodeBuilder = NavArgumentBridgeCodeBuilder(
        navArgResolver = navArgResolver,
        importableHelper = importableHelper,
        navArgs = navGraph.navArgs?.parameters.orEmpty(),
        errorLocationPrefix = navGraph.name
    )

    fun write() {
        val startRouteInfo = importableHelper.startingRouteInfo(navGraph)
        val file = moduleNavGraphTemplate.sourceCode
            .replace(
                USER_NAV_GRAPH_ANNOTATION,
                importableHelper.addAndGetPlaceholder(navGraph.annotationType)
            )
            .replace(NAV_GRAPH_NAME_PLACEHOLDER, navGraph.name)
            .replace(NAV_GRAPH_ROUTE_PLACEHOLDER, navGraph.graphRouteCode())
            .replace(NAV_GRAPH_INVOKE_FUNCTION, navGraph.graphInvokeFunction())
            .replace(NAV_GRAPH_ARGS_FROM, navGraph.argsFromFunctions(startRouteInfo.isDestination))
            .replace(NAV_GRAPH_ARGUMENTS_PLACEHOLDER, navGraph.navArgumentsCode())
            .replace(NAV_GRAPH_DEEP_LINKS_PLACEHOLDER, navGraph.deepLinksCode())
            .replace(INNER_IMPORTED_ROUTES, navGraph.innerExternalRoutes())
            .replace(NAV_GRAPH_GEN_NAV_ARGS, navGraph.generatedNavArgsClass())
            .replace(
                NAV_GRAPH_VISIBILITY_PLACEHOLDER,
                navGraph.visibility.let {
                    when (it) {
                        Visibility.PUBLIC -> """
                            @${
                            importableHelper.addAndGetPlaceholder(
                                Importable(
                                    "Keep",
                                    "androidx.annotation.Keep"
                                )
                            )
                        }
                            ${it.name.lowercase()}
                        """.trimIndent()

                        Visibility.INTERNAL,
                        Visibility.PRIVATE -> it.name.lowercase()
                    }
                }
            )
            .replace(
                NAV_GRAPH_TYPE,
                navGraph.graphSuperType()
            )
            .replace(
                NAV_GRAPH_START_TYPED_ROUTE_TYPE,
                navGraph.startRouteType(startRouteInfo.isDestination)
            )
            .replace(
                NAV_GRAPH_START_ROUTE_PLACEHOLDER,
                startRouteInfo.name
            )
            .replace(
                NAV_GRAPH_DESTINATIONS,
                navGraphDestinationsCode(navGraph.destinations, navGraph.externalDestinations)
            )
            .replace(
                NAV_GRAPH_KDOC,
                NavGraphsPrettyKdocWriter(
                    importableHelper,
                    listOf(navGraph)
                ).write(includeLegend = false)
            )
            .replace(
                REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER,
                importableHelper.requireOptInAnnotations(navGraph.destinations.flatMap { it.requireOptInAnnotationTypes } + navGraph.requireOptInAnnotationTypes)
            )
            .replace(
                NESTED_NAV_GRAPHS,
                nestedNavGraphsCode(navGraph.nestedGraphs, navGraph.externalNavGraphs)
            )
            .replace(
                NAV_GRAPH_DEFAULT_TRANSITIONS_TYPE,
                if (navGraph.isNavHostGraph) {
                    importableHelper.addAndGetPlaceholder(CORE_NAV_HOST_ANIMATED_DESTINATION_STYLE)
                } else {
                    "$CORE_DESTINATION_ANIMATION_STYLE${if (navGraph.defaultTransitions == null) "?" else ""}"
                }
            )
            .replace(
                NAV_GRAPH_DEFAULT_TRANSITIONS,
                navGraph.defaultTransitions?.let {
                    importableHelper.addAndGetPlaceholder(it)
                } ?: "null"
            ).let {
                if (navGraph.destinations.isEmpty()) {
                    importableHelper.remove(setOfImportable("${codeGenBasePackageName}.destinations.*"))
                }
                it
            }

        codeGenerator.makeFile(
            packageName = navGraphsPackageName,
            name = navGraph.name,
            sourceIds = sourceIds(navGraph.destinations, listOf(navGraph)).toTypedArray(),
        )
            .writeSourceFile(
                packageStatement = moduleNavGraphTemplate.packageStatement,
                importableHelper = importableHelper,
                sourceCode = file,
                fileOptIns = setOf(
                    Importable("InternalDestinationsApi", "com.ramcosta.composedestinations.annotation.internal.InternalDestinationsApi")
                )
            )
    }

    private fun RawNavGraphTree.navArgumentsCode(): String {
        val startRouteArguments = if (startRouteArgs != null) {
            " + startRoute.arguments"
        } else {
            ""
        }

        if (navArgs == null && startRouteArgs != null) {
            return "\n\n\toverride val arguments: List<${importableHelper.addAndGetPlaceholder(navArgumentBridgeCodeBuilder.navClassArgumentImportable)}> get() = startRoute.arguments"
        }

        return "\n" + navArgumentBridgeCodeBuilder.navArgumentsDeclarationCode(startRouteArguments)
    }

    private fun RawNavGraphTree.graphInvokeFunction(): String {
        if (isNavHostGraph || hasNoArgs()) {
            return "\n"
        }

        if (usesSameArgsAsStartRoute()) {
            return """
            |
            |
            |    override fun invoke(navArgs: ${importableHelper.addAndGetPlaceholder(navArgTypes.second!!)}): $CORE_DIRECTION {
            |        return $CORE_DIRECTION(
            |            route = baseRoute + startRoute(navArgs).route.removePrefix(startRoute.baseRoute)
            |        )
            |    }    
            """.trimMargin()
        }

        val navArgsType = navArgTypes.first?.let { importableHelper.addAndGetPlaceholder(it) } ?: "Unit"
        val directionRouteSuffix = if (navArgTypes.second != null) {
            " + \n\t\t\t\t\t\"\${startRoute(startRouteArgs).route.removePrefix(startRoute.baseRoute)}\""
        } else {
            ""
        }

        return navArgumentBridgeCodeBuilder.invokeMethodsCode(
            navArgsType = navArgsType,
            writeEmptyInvoke = false,
            directionRouteSuffix = directionRouteSuffix,
            additionalArgs = navArgTypes.second?.let {
                mapOf("startRouteArgs" to it)
            } ?: emptyMap()
        ).let {
            if (it.isNotEmpty()) "\n$it"
            else it
        }
    }

    private fun RawNavGraphTree.argsFromFunctions(isStartRouteDestination: Boolean): String {
        if (usesSameArgsAsStartRoute()) {
            val startRouteArgsTypePlaceHolder = importableHelper.addAndGetPlaceholder(navArgTypes.second!!)
            return """
            |
            |    override fun argsFrom(bundle: ${importableHelper.addAndGetPlaceholder(bundleImportable)}?): $startRouteArgsTypePlaceHolder? {
            |        return startRoute.argsFrom(bundle)
            |    }
            |
            |    override fun argsFrom(savedStateHandle: ${importableHelper.addAndGetPlaceholder(savedStateHandleImportable)}): $startRouteArgsTypePlaceHolder? {
            |        return startRoute.argsFrom(savedStateHandle)
            |    }    
            """.trimMargin()
        }

        val navArgsType = navArgTypes.first?.let { importableHelper.addAndGetPlaceholder(it) } ?: "Unit"
        val startRouteArgsLine: (String) -> String? = if (navArgTypes.second != null) {
            val startRouteArgsSuffix = if(isStartRouteDestination) {
                ""
            } else {
                " ?: return null"
            }
            { "\n\t\tstartRouteArgs = startRoute.argsFrom($it)$startRouteArgsSuffix," }
        } else {
            { null }
        }

        return navArgumentBridgeCodeBuilder.argsFromFunctions("$navArgsType?", startRouteArgsLine) {
            " ?: return null"
        }
    }

    private fun RawNavGraphTree.generatedNavArgsClass(): String {
        if (requiresGeneratingNavArgsClass()) {
            val code = StringBuilder()
            code += "/**\n"
            code += " * Generated to have args containing both [${importableHelper.addAndGetPlaceholder(navArgs!!.type)}] and [${navArgTypes.second!!.preferredSimpleName}]\n"
            code += " **/\n"
            code += "${getGenNavArgsClassVisibility()} data class ${navGraph.name}Args(\n"
            code += "${navArgumentBridgeCodeBuilder.innerNavArgsParametersCode("\tval ")}\n"
            code += "\tval startRouteArgs: ${importableHelper.addAndGetPlaceholder(navArgTypes.second!!)}\n"
            code += ")\n\n"
            return code.toString()
        }

        return ""
    }

    private fun getGenNavArgsClassVisibility(): String {
        if (navGraph.visibility == Visibility.PUBLIC) {
            return "public"
        }

        if (navGraph.annotationType in setOfPublicStartParticipatingTypes) {
            return "public"
        }

        return "internal"
    }

    private fun RawNavGraphTree.graphRouteCode(): String {
        if (isNavHostGraph || (navArgs == null && startRouteArgs == null)) {
            return """
                override val route: String = "$baseRoute"
            """.trimIndent()
                .prependIndent("\t")
        }

        val startRouteArgsRoute = if (startRouteArgs != null) {
            "\${startRoute.route.removePrefix(startRoute.baseRoute)}"
        } else {
            ""
        }

        return """
            override val baseRoute: String = "$baseRoute"
            
            override val route: String = "${navArgumentBridgeCodeBuilder.constructRoute(true)}$startRouteArgsRoute"
        """.trimIndent()
            .prependIndent("\t")
    }

    private fun RawNavGraphTree.graphSuperType(): String {
        if (isNavHostGraph) {
            return if (startRouteArgs != null) {
                "${importableHelper.addAndGetPlaceholder(typedNavHostNavGraphType)}<${importableHelper.addAndGetPlaceholder(startRouteArgs.type)}>"
            } else {
                importableHelper.addAndGetPlaceholder(directionNavHostNavGraphType)
            }
        }

        val (graphArgs, startRouteNavArgsName) = navArgTypes.let { argTypes ->
            argTypes.first?.let { importableHelper.addAndGetPlaceholder(it) } to argTypes.second?.let { importableHelper.addAndGetPlaceholder(it) }
        }

        return if (graphArgs != null || startRouteNavArgsName != null) {
            val startRouteArgType = startRouteNavArgsName ?: "Unit"
            val argTypes = "${graphArgs ?: startRouteArgType}, $startRouteArgType"
            "${importableHelper.addAndGetPlaceholder(navGraphType)}<$argTypes>"
        } else {
            importableHelper.addAndGetPlaceholder(directionNavGraphType)
        }
    }

    private fun RawNavGraphTree.startRouteType(isDestination: Boolean): String {
        if (isNavHostGraph) {
            return if (startRouteArgs != null) {
                "TypedRoute<${importableHelper.addAndGetPlaceholder(startRouteArgs.type)}>"
            } else {
                "TypedRoute<Unit>"
            }
        }

        return if (startRouteArgs != null) {
            if (isDestination) {
                "TypedDestinationSpec<${importableHelper.addAndGetPlaceholder(startRouteArgs.type)}>"
            } else {
                "TypedNavGraphSpec<${importableHelper.addAndGetPlaceholder(startRouteArgs.type)}, *>"
            }
        } else {
            "TypedRoute<Unit>"
        }
    }

    private fun navGraphDestinationsCode(
        destinations: List<CodeGenProcessedDestination>,
        externalDestinations: List<ExternalRoute.Destination>,
    ): String {
        val allDestinations = destinations.map { it.destinationImportable.simpleName } +
                    externalDestinations.map {
                        if (it.canUseOriginal()) importableHelper.addAndGetPlaceholder(it.generatedType)
                        else "External_${it.generatedType.preferredSimpleName}"
                    }

        val code = StringBuilder()
        allDestinations.forEachIndexed { idx, it ->
            code += "\t\t${it}"

            if (idx != allDestinations.lastIndex) {
                code += ",\n"
            }
        }

        return code.toString()
    }

    private fun ImportableHelper.requireOptInAnnotations(requireOptInClassTypes: List<Importable>): String {
        val code = StringBuilder()

        requireOptInClassTypes.toSet().forEach { annotationType ->
            code += "@${addAndGetPlaceholder(annotationType)}\n"
        }

        return code.toString()
    }

    private fun nestedNavGraphsCode(
        nestedNavGraphs: List<NavGraphGenParams>,
        externalNestedGraphs: List<ExternalRoute.NavGraph>
    ): String {
        if (nestedNavGraphs.isEmpty() && externalNestedGraphs.isEmpty()) {
            return ""
        }

        val allNestedGraphs = nestedNavGraphs.map { it.name } +
                externalNestedGraphs.map {
                    if (it.canUseOriginal()) importableHelper.addAndGetPlaceholder(it.generatedType)
                    else "External_${it.generatedType.preferredSimpleName}"
                }

        return """


            override val nestedNavGraphs: List<$CORE_ALIAS_NAV_GRAPH_SPEC> get() = listOf(
                %s1
            )
        """.trimIndent()
            .prependIndent("\t")
            .replace("%s1", allNestedGraphs.joinToString(", \n\t\t"))
    }

    private fun RawNavGraphTree.deepLinksCode(): String {
        return "\n" + navArgumentBridgeCodeBuilder.deepLinksDeclarationCode(deepLinks, fullRoutePlaceholderReplacement = ::navGraphFullRoutReplacement)
    }

    private fun navGraphFullRoutReplacement(@Suppress("UNUSED_PARAMETER") params: List<Parameter>): String {
        // For now we'll try just using the full route here, even if some args were removed from being not mandatory
        return "\$route"
    }

    private val externalRouteTemplate = StringBuilder()
        .appendLine()
        .append("\tprivate object External_@ROUTE_NAME@ : ExternalRoute(), @ROUTE_SUPER_TYPE@ by @IMPORTED_GEN_ROUTE_CLASS@ {")
        .appendLine("@INSIDE_CODE@")
        .append("\t}")
        .toString()

    private fun RawNavGraphTree.innerExternalRoutes(): String {
        return externalRoutes.joinToString("\n") {
            when (it) {
                is ExternalRoute.Destination -> it.generatedExternaldDestinationCode()
                is ExternalRoute.NavGraph -> it.generatedExternaldNavGraphCode()
            }
        }
    }

    private fun ExternalRoute.Destination.generatedExternaldDestinationCode(): String {
        if (canUseOriginal()) {
            return ""
        }

        val insideCode = StringBuilder().apply {
            append("\n\n\t\toverride val original = ${importableHelper.addAndGetPlaceholder(generatedType)}")

            if (overriddenDestinationStyleType != null) {
                append("\n\n\t\toverride val style get() = ${overriddenDestinationStyleType.code(importableHelper)}")
            }

            if (additionalDeepLinks.isNotEmpty()) {
                val navArgumentBridgeCodeBuilder = NavArgumentBridgeCodeBuilder(
                    importableHelper,
                    navArgResolver,
                    navArgs?.parameters.orEmpty(),
                    navGraph.name
                )
                append("\n\n\t\toverride val deepLinks get() = ${navArgumentBridgeCodeBuilder.deepLinksDeclarationCode(additionalDeepLinks, listOfOnly = true, innerTabsCount = 3) {
                    navArgumentBridgeCodeBuilder.constructRoute(false, it, "\${${importableHelper.addAndGetPlaceholder(generatedType)}.baseRoute}")
                }}")
                append(" + ${importableHelper.addAndGetPlaceholder(generatedType)}.deepLinks")
            }

            if (additionalComposableWrappers.isNotEmpty()) {
                importableHelper.add(Importable("Composable", "androidx.compose.runtime.Composable"))
                importableHelper.add(Importable("Wrap", "$CORE_PACKAGE_NAME.wrapper.Wrap"))
                importableHelper.add(Importable("DestinationScope", "$CORE_PACKAGE_NAME.scope.DestinationScope"))
                append("\n\n")
                appendLine("\t\t@Composable")
                val typeArgImportable = superType.typeArguments.firstOrNull()?.let {
                    (it as TypeArgument.Typed).type.importable
                }
                val typeArgCode = if (typeArgImportable == null) {
                    "Unit"
                } else {
                    importableHelper.addAndGetPlaceholder(typeArgImportable)
                }

                appendLine("\t\toverride fun DestinationScope<$typeArgCode>.Content() {")
                appendLine("\t\t\tWrap(${additionalComposableWrappers.joinToString(", ") { importableHelper.addAndGetPlaceholder(it) }}) {")
                appendLine("\t\t\t\twith(${importableHelper.addAndGetPlaceholder(generatedType)}) { Content() }")
                appendLine("\t\t\t}")
                appendLine("\t\t}")
            }
        }

        return externalRouteTemplate
            .replace("@ROUTE_NAME@", generatedType.preferredSimpleName)
            .replace("@ROUTE_SUPER_TYPE@", superType.toTypeCode(importableHelper))
            .replace("@IMPORTED_GEN_ROUTE_CLASS@", importableHelper.addAndGetPlaceholder(generatedType))
            .replace("@INSIDE_CODE@", insideCode.toString())
    }

    private fun ExternalRoute.NavGraph.generatedExternaldNavGraphCode(): String {
        if (canUseOriginal()) {
            return ""
        }

        val insideCode = StringBuilder().apply {
            append("\n\n\t\toverride val original = ${importableHelper.addAndGetPlaceholder(generatedType)}")

            if (additionalDeepLinks.isNotEmpty()) {
                val navArgumentBridgeCodeBuilder = NavArgumentBridgeCodeBuilder(
                    importableHelper,
                    navArgResolver,
                    navArgs?.parameters.orEmpty(),
                    navGraph.name
                )
                append("\n\n\t\toverride val deepLinks get() = ")

                append(
                    "${
                        navArgumentBridgeCodeBuilder.deepLinksDeclarationCode(
                            deepLinks = additionalDeepLinks,
                            listOfOnly = true,
                            innerTabsCount = 3,
                            fullRoutePlaceholderReplacement = {
                                "\${${importableHelper.addAndGetPlaceholder(generatedType)}.route}"
                            }
                        )
                    } + ${importableHelper.addAndGetPlaceholder(generatedType)}.deepLinks"
                )
            }

            when (overriddenDefaultTransitions) {
                is ExternalRoute.NavGraph.OverrideDefaultTransitions.NoOverride -> Unit
                is ExternalRoute.NavGraph.OverrideDefaultTransitions.Override -> {
                    append("\n\n\t\toverride val defaultTransitions get() = ${overriddenDefaultTransitions.importable?.let { importableHelper.addAndGetPlaceholder(it) } ?: "null"}")
                }
            }
        }

        return externalRouteTemplate
            .replace("@ROUTE_NAME@", generatedType.preferredSimpleName)
            .replace("@ROUTE_SUPER_TYPE@", superType.toTypeCode(importableHelper))
            .replace("@IMPORTED_GEN_ROUTE_CLASS@", importableHelper.addAndGetPlaceholder(generatedType))
            .replace("@INSIDE_CODE@", insideCode.toString())
    }
}

