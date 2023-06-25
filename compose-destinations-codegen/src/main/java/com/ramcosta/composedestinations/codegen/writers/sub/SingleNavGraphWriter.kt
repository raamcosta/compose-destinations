package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_ALIAS_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_DESTINATION_ANIMATION_STYLE
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_NAV_HOST_ANIMATED_DESTINATION_STYLE
import com.ramcosta.composedestinations.codegen.commons.CORE_NAV_HOST_GRAPH_SPEC
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
import com.ramcosta.composedestinations.codegen.model.NavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_ARGS_FROM
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_ARGUMENTS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_DEEP_LINKS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_DEFAULT_TRANSITIONS
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_DEFAULT_TRANSITIONS_TYPE
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_DESTINATIONS
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_GEN_NAV_ARGS
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_INVOKE_FUNCTION
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_NAME_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_START_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_TYPE
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_TYPED_ROUTE_TYPE
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_VISIBILITY_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NESTED_NAV_GRAPHS
import com.ramcosta.composedestinations.codegen.templates.REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.USER_NAV_GRAPH_ANNOTATION
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
            .replace(USER_NAV_GRAPH_ANNOTATION, importableHelper.addAndGetPlaceholder(navGraph.type))
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
                startingDestinationName(navGraph)
            )
            .replace(
                NAV_GRAPH_DESTINATIONS,
                navGraphDestinationsCode(navGraph.destinations)
            )
            .replace(
                REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER,
                importableHelper.requireOptInAnnotations(navGraph.destinations)
            )
            .replace(
                NESTED_NAV_GRAPHS,
                nestedNavGraphsCode(navGraph.nestedGraphs)
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
            )

        codeGenerator.makeFile(
            packageName = navGraphsPackageName,
            name = navGraph.name,
            sourceIds = sourceIds(navGraph.destinations, listOf(navGraph)).toTypedArray()
        )
            .writeSourceFile(
                packageStatement = moduleNavGraphTemplate.packageStatement,
                importableHelper = importableHelper,
                sourceCode = file
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
        val navArgTypes = navArgTypes()

        if (navArgTypes.first == navArgTypes.second && navArgTypes.second != null) {
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

        if (isNavHostGraph || navArgTypes.first == null && navArgTypes.second == null) {
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
        val navArgTypes = navArgTypes()

        if (navArgTypes.first == navArgTypes.second && navArgTypes.second != null) {
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

    private fun RawNavGraphTree.deepLinksCode(): String {
        return "\n" + navArgumentBridgeCodeBuilder.deepLinksDeclarationCode(deepLinks) {
            // For now we'll try just using the full route here, even if some args were removed from being not mandatory
            "\$route"
        }
    }

    private fun RawNavGraphTree.generatedNavArgsClass(): String {
        val argTypes = navArgTypes()

        if (argTypes.first != null && argTypes.second != null && argTypes.first != argTypes.second) {
            val code = StringBuilder()
            code += "${getGenNavArgsClassVisibility()} data class ${navGraph.name}NavArgs(\n"
            code += "${navArgumentBridgeCodeBuilder.innerNavArgsParametersCode("\tval ")}\n"
            code += "\tval startRouteArgs: ${importableHelper.addAndGetPlaceholder(argTypes.second!!)}\n"
            code += ")\n\n"
            return code.toString()
        }

        return ""
    }

    private fun getGenNavArgsClassVisibility(): String {
        if (navGraph.visibility == Visibility.PUBLIC) {
            return "public"
        }

        if (navGraph.type in setOfPublicStartParticipatingTypes) {
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

        val (graphArgs, startRouteNavArgsName) = navArgTypes().let { argTypes ->
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

    private fun RawNavGraphTree.navArgTypes(): Pair<Importable?, Importable?> {
        return (graphArgs ?: startRouteArgs?.type) to startRouteArgs?.type
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

    private fun navGraphDestinationsCode(destinations: List<CodeGenProcessedDestination>): String {
        val code = StringBuilder()
        destinations.forEachIndexed { idx, it ->
            code += "\t\t${it.destinationImportable.simpleName}"

            if (idx != destinations.lastIndex) {
                code += ",\n"
            }
        }

        return code.toString()
    }


    private fun ImportableHelper.requireOptInAnnotations(generatedDestinations: List<CodeGenProcessedDestination>): String {
        val requireOptInClassTypes =
            generatedDestinations.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            code += "@${addAndGetPlaceholder(annotationType)}\n"
        }

        return code.toString()
    }

    private fun nestedNavGraphsCode(nestedNavGraphs: List<NavGraphGenParams>): String {
        if (nestedNavGraphs.isEmpty()) {
            return ""
        }

        return """


            override val nestedNavGraphs: List<$CORE_ALIAS_NAV_GRAPH_SPEC> get() = listOf(
                %s1
            )
        """.trimIndent()
            .prependIndent("\t")
            .replace("%s1", nestedNavGraphs.joinToString(", \n\t\t") { it.name })
    }
}

