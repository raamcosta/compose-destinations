package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_ALIAS_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_DESTINATION_ANIMATION_STYLE
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_NAV_HOST_ANIMATED_DESTINATION_STYLE
import com.ramcosta.composedestinations.codegen.commons.CORE_NAV_HOST_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.CORE_TYPED_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.bundleImportable
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.savedStateHandleImportable
import com.ramcosta.composedestinations.codegen.commons.setOfPublicStartParticipatingTypes
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.commons.startingDestinationName
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.IncludedRoute
import com.ramcosta.composedestinations.codegen.model.NavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.Visibility
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
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_TYPE
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_TYPED_ROUTE_TYPE
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
    private val navHostNavGraphType = CORE_NAV_HOST_GRAPH_SPEC
    private val directionNavGraphType = CORE_DIRECTION_NAV_GRAPH_SPEC

    private val navArgumentBridgeCodeBuilder = NavArgumentBridgeCodeBuilder(
        navArgResolver = navArgResolver,
        importableHelper = importableHelper,
        navArgs = navGraph.navArgs?.parameters.orEmpty(),
        errorLocationPrefix = navGraph.name
    )

    fun write() {
        val file = moduleNavGraphTemplate.sourceCode
            .replace(USER_NAV_GRAPH_ANNOTATION, importableHelper.addAndGetPlaceholder(navGraph.annotationType))
            .replace(NAV_GRAPH_NAME_PLACEHOLDER, navGraph.name)
            .replace(NAV_GRAPH_ROUTE_PLACEHOLDER, navGraph.graphRouteCode())
            .replace(NAV_GRAPH_INVOKE_FUNCTION, navGraph.graphInvokeFunction())
            .replace(NAV_GRAPH_ARGS_FROM, navGraph.argsFromFunctions())
            .replace(NAV_GRAPH_ARGUMENTS_PLACEHOLDER, navGraph.navArgumentsCode())
            .replace(NAV_GRAPH_DEEP_LINKS_PLACEHOLDER, navGraph.deepLinksCode())
            .replace(NAV_GRAPH_GEN_NAV_ARGS, navGraph.generatedNavArgsClass())
            .replace(NAV_GRAPH_VISIBILITY_PLACEHOLDER, navGraph.visibility.name.lowercase())
            .replace(
                NAV_GRAPH_TYPE,
                navGraph.graphSuperType()
            )
            .replace(
                NAV_GRAPH_TYPED_ROUTE_TYPE,
                navGraph.startRouteType()
            )
            .replace(
                NAV_GRAPH_START_ROUTE_PLACEHOLDER,
                importableHelper.startingDestinationName(navGraph)
            )
            .replace(
                NAV_GRAPH_DESTINATIONS,
                navGraphDestinationsCode(navGraph.destinations, navGraph.importedDestinations)
            )
            .replace(
                NAV_GRAPH_KDOC,
                NavGraphsPrettyKdocWriter(importableHelper, listOf(navGraph)).write(includeLegend = false)
            )
            .replace(
                REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER,
                importableHelper.requireOptInAnnotations(navGraph.destinations.flatMap { it.requireOptInAnnotationTypes } + navGraph.requireOptInAnnotationTypes)
            )
            .replace(
                NESTED_NAV_GRAPHS,
                nestedNavGraphsCode(navGraph.nestedGraphs, navGraph.importedNavGraphs)
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
                    Importable("InternalDestinationsApi", "com.ramcosta.composedestinations.annotation.InternalDestinationsApi")
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

        if (isNavHostGraph || hasNoArgs()) {
            return ""
        }

        val navArgsType = navArgTypes.first?.let { importableHelper.addAndGetPlaceholder(it) } ?: "Unit"
        val directionRouteSuffix = if (navArgTypes.second != null) {
            " + \n\t\t\t\t\t\"\${startRoute(startRouteArgs).route.removePrefix(startRoute.baseRoute)}\""
        } else {
            ""
        }

        return navArgumentBridgeCodeBuilder.invokeMethodsCode(
            navArgsType,
            false,
            directionRouteSuffix,
            navArgTypes.second?.let {
                mapOf("startRouteArgs" to it)
            } ?: emptyMap()
        ).let {
            if (it.isNotEmpty()) "\n$it"
            else it
        }
    }

    private fun RawNavGraphTree.argsFromFunctions(): String {
        if (usesSameArgsAsStartRoute()) {
            val startRouteArgsTypePlaceHolder = importableHelper.addAndGetPlaceholder(navArgTypes.second!!)
            return """
            |
            |    override fun argsFrom(bundle: ${importableHelper.addAndGetPlaceholder(bundleImportable)}?): $startRouteArgsTypePlaceHolder {
            |        return startRoute.argsFrom(bundle)
            |    }
            |
            |    override fun argsFrom(savedStateHandle: ${importableHelper.addAndGetPlaceholder(savedStateHandleImportable)}): $startRouteArgsTypePlaceHolder {
            |        return startRoute.argsFrom(savedStateHandle)
            |    }    
            """.trimMargin()
        }

        val navArgsType = navArgTypes.first?.let { importableHelper.addAndGetPlaceholder(it) } ?: "Unit"
        val startRouteArgsLine: (String) -> String? = if (navArgTypes.second != null) {
            { "\n\t\tstartRouteArgs = startRoute.argsFrom($it)," }
        } else {
            { null }
        }

        return navArgumentBridgeCodeBuilder.argsFromFunctions(navArgsType, startRouteArgsLine)
    }

    private fun RawNavGraphTree.generatedNavArgsClass(): String {
        if (requiresGeneratingNavArgsClass()) {
            val code = StringBuilder()
            code += "/**\n"
            code += " * Generated to have args containing both [${importableHelper.addAndGetPlaceholder(navArgs!!.type)}] and [${navArgTypes.second!!.preferredSimpleName}]\n"
            code += " **/\n"
            code += "${getGenNavArgsClassVisibility()} data class ${navGraph.name}NavArgs(\n"
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
            return importableHelper.addAndGetPlaceholder(navHostNavGraphType)
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

    private fun RawNavGraphTree.startRouteType(): String {
        if (isNavHostGraph) {
            return "Unit"
        }

        return if (startRouteArgs != null) {
            importableHelper.addAndGetPlaceholder(startRouteArgs.type)
        } else {
            "Unit"
        }
    }

    private fun navGraphDestinationsCode(
        destinations: List<CodeGenProcessedDestination>,
        importedDestinations: List<IncludedRoute.Destination>,
    ): String {
        val allDestinations = destinations.map { it.destinationImportable.simpleName } +
                    importedDestinations.map { it.importedDestinationCode() }

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
        importedNestedGraphs: List<IncludedRoute.NavGraph>
    ): String {
        if (nestedNavGraphs.isEmpty() && importedNestedGraphs.isEmpty()) {
            return ""
        }

        val allNestedGraphs = nestedNavGraphs.map { it.name } +
                importedNestedGraphs.map { it.importedNavGraphCode() }

        return """


            override val nestedNavGraphs: List<$CORE_ALIAS_NAV_GRAPH_SPEC> get() = listOf(
                %s1
            )
        """.trimIndent()
            .prependIndent("\t")
            .replace("%s1", allNestedGraphs.joinToString(", \n\t\t"))
    }

    private fun IncludedRoute.Destination.importedDestinationCode(): String {
        if (additionalComposableWrappers.isEmpty() && overriddenDestinationStyleType == null && additionalDeepLinks.isEmpty()) {
            return importableHelper.addAndGetPlaceholder(generatedType)
        }

        importableHelper.addPriorityQualifiedImport(Importable("with", "$CORE_PACKAGE_NAME.dynamic.destination.with"))


        return StringBuilder().apply {
            appendLine("${importableHelper.addAndGetPlaceholder(generatedType)}.with {")

            if (overriddenDestinationStyleType != null) {
                appendLine("\t\t\tstyle = ${overriddenDestinationStyleType.code(importableHelper)}")
            }

            if (additionalComposableWrappers.isNotEmpty()) {
                appendLine("\t\t\tadditionalWrappers = arrayOf(${additionalComposableWrappers.joinToString(", ") { importableHelper.addAndGetPlaceholder(it) }})")
            }

            if (additionalDeepLinks.isNotEmpty()) {
                val navArgumentBridgeCodeBuilder = NavArgumentBridgeCodeBuilder(
                    importableHelper,
                    navArgResolver,
                    this@importedDestinationCode.navArgs?.parameters.orEmpty(),
                    navGraph.name
                )
                appendLine("\t\t\tadditionalDeepLinks = ${navArgumentBridgeCodeBuilder.deepLinksDeclarationCode(additionalDeepLinks, listOfOnly = true, innerTabsCount = 4) {
                    navArgumentBridgeCodeBuilder.constructRoute(false, it, "\${${importableHelper.addAndGetPlaceholder(generatedType)}.baseRoute}")
                }}")
            }
            append("\t\t}")
        }.toString()
    }

    private fun IncludedRoute.NavGraph.importedNavGraphCode(): String {
        if (additionalDeepLinks.isEmpty() && overriddenDefaultTransitions is IncludedRoute.NavGraph.OverrideDefaultTransitions.NoOverride) {
            return importableHelper.addAndGetPlaceholder(generatedType)
        }

        importableHelper.addPriorityQualifiedImport(Importable("with", "$CORE_PACKAGE_NAME.dynamic.navgraph.with"))

        return StringBuilder().apply {
            appendLine("${importableHelper.addAndGetPlaceholder(generatedType)}.with {")

            if (overriddenDefaultTransitions is IncludedRoute.NavGraph.OverrideDefaultTransitions.Override) {
                appendLine("\t\t\tdefaultTransitions = ${overriddenDefaultTransitions.importable?.let { importableHelper.addAndGetPlaceholder(it) } ?: "null"}")
            }

            if (additionalDeepLinks.isNotEmpty()) {
                val navArgumentBridgeCodeBuilder = NavArgumentBridgeCodeBuilder(
                    importableHelper,
                    navArgResolver,
                    this@importedNavGraphCode.navArgs?.parameters.orEmpty(),
                    navGraph.name
                )
                appendLine("\t\t\tadditionalDeepLinks = ${navArgumentBridgeCodeBuilder.deepLinksDeclarationCode(additionalDeepLinks, listOfOnly = true, innerTabsCount = 4) {
                    "\${${importableHelper.addAndGetPlaceholder(generatedType)}.route}"
                }}")
            }

            append("\t\t}")
        }.toString()
    }

    private fun RawNavGraphTree.deepLinksCode(): String {
        return "\n" + navArgumentBridgeCodeBuilder.deepLinksDeclarationCode(deepLinks, fullRoutePlaceholderReplacement = ::navGraphFullRoutReplacement)
    }

    private fun navGraphFullRoutReplacement(@Suppress("UNUSED_PARAMETER") params: List<Parameter>): String {
        // For now we'll try just using the full route here, even if some args were removed from being not mandatory
        return "\$route"
    }
}

