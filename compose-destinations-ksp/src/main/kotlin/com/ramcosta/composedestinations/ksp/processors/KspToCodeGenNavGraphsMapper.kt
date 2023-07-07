package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_ROUTE_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION_DEFAULT_NAME
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_GRAPH_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_GRAPH_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.model.ExternalRoute
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.NavTypeSerializer
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.ksp.commons.MutableKSFileSourceMapper
import com.ramcosta.composedestinations.ksp.commons.findActualClassDeclaration
import com.ramcosta.composedestinations.ksp.commons.findAllRequireOptInAnnotations
import com.ramcosta.composedestinations.ksp.commons.findArgumentValue
import com.ramcosta.composedestinations.ksp.commons.getNavArgsDelegateType
import com.ramcosta.composedestinations.ksp.commons.isNothing
import com.ramcosta.composedestinations.ksp.commons.toDeepLink
import com.ramcosta.composedestinations.ksp.commons.toGenVisibility
import com.ramcosta.composedestinations.ksp.commons.toImportable

internal class KspToCodeGenNavGraphsMapper(
    private val resolver: Resolver,
    private val mutableKSFileSourceMapper: MutableKSFileSourceMapper,
    private val navTypeSerializersByType: Map<Importable, NavTypeSerializer>,
) {

    fun map(
        navGraphAnnotations: Sequence<KSClassDeclaration>,
        navHostGraphAnnotations: Sequence<KSClassDeclaration>
    ): List<RawNavGraphGenParams> {
        return navGraphAnnotations.map { it.mapToRawNavGraphGenParams(false) }.toList() +
                navHostGraphAnnotations.map { it.mapToRawNavGraphGenParams(true) }
    }

    private fun KSClassDeclaration.mapToRawNavGraphGenParams(isNavHostGraph: Boolean): RawNavGraphGenParams {
        if (classKind != KSPClassKind.ANNOTATION_CLASS) {
            throw IllegalDestinationsSetup("Classes annotated with `@NavGraph` must be annotation classes!")
        }

        primaryConstructor?.parameters?.firstOrNull {
            it.name?.asString() == "start" &&
                    it.type.toString() == "Boolean" &&
                    it.hasDefault
        }
            ?: throw IllegalDestinationsSetup(
                "Classes annotated with `@NavGraph` must contain " +
                        "a single parameter like: `val start: Boolean = false`!"
            )

        val navGraphAnnotation = if (isNavHostGraph) {
            annotations.first {
                it.shortName.asString() == NAV_HOST_GRAPH_ANNOTATION
            }
        } else {
            annotations.first {
                it.shortName.asString() == NAV_GRAPH_ANNOTATION
            }
        }

        val navGraphAnnotationNameArg = navGraphAnnotation
            .findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)
        val navGraphAnnotationDefaultArg = navGraphAnnotation
            .findArgumentValue<Boolean>("default")!!
        val navGraphVisibility = navGraphAnnotation
            .findArgumentValue<KSType>("visibility")!!
            .toGenVisibility()
        val navGraphDefaultTransitions = navGraphAnnotation
            .findArgumentValue<KSType>("defaultTransitions")
            ?.findActualClassDeclaration()
            ?.takeIf { !it.isNothing }
            ?.toImportable()

        val deepLinks = navGraphAnnotation
            .findArgumentValue<ArrayList<KSAnnotation>>(DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT)
            ?.map { it.toDeepLink() }
            .orEmpty()

        val externalRoutesAnnotation = annotations.find { it.shortName.asString() == "ExternalRoutes" }

        val externalNavGraphs = getExternalNavGraphs(externalRoutesAnnotation)
        val externalDestinations = getExternalDestinations(externalRoutesAnnotation)
        val externalStartRoute = getExternalStartRoute(externalRoutesAnnotation, externalNavGraphs, externalDestinations)

        val navArgs = navGraphAnnotation
            .getNavArgsDelegateType(resolver, navTypeSerializersByType)

        if (isNavHostGraph && navGraphDefaultTransitions == null) {
            throw IllegalDestinationsSetup(
                "A $NAV_HOST_GRAPH_ANNOTATION needs a non Nothing::class as defaultTransitions! " +
                        "Use `NoTransitions` if you wish to have no animations as default for this nav graph ${simpleName.asString()}"
            )
        }

        var parentGraphAnnotationResolved: KSType? = null
        val parentGraphAnnotation = annotations.firstOrNull { annotationOfAnnotation ->
            if (annotationOfAnnotation.shortName.asString() == NAV_GRAPH_ANNOTATION) return@firstOrNull false

            val resolved = annotationOfAnnotation.annotationType.resolve()
            resolved.declaration.annotations.any {
                val annotationShortName = it.shortName.asString()
                if (annotationShortName != NAV_GRAPH_ANNOTATION && annotationShortName != NAV_HOST_GRAPH_ANNOTATION) {
                    return@any false
                }

                val annotationQualifiedName =
                    it.annotationType.resolve().declaration.qualifiedName?.asString()
                annotationQualifiedName == NAV_GRAPH_ANNOTATION_QUALIFIED
                        || annotationQualifiedName == NAV_HOST_GRAPH_ANNOTATION_QUALIFIED
            }.also {
                if (it) parentGraphAnnotationResolved = resolved
            }
        }

        val isParentStart = parentGraphAnnotation?.arguments?.first()?.value as? Boolean?
        val parent = parentGraphAnnotationResolved?.let {
            Importable(
                it.declaration.simpleName.asString(),
                it.declaration.qualifiedName!!.asString()
            )
        }

        if (isNavHostGraph && parent != null) {
            throw IllegalDestinationsSetup("NavHostGraph's cannot have a parent navigation graph! (${simpleName.asString()}, parent = ${parent.simpleName})")
        }

        containingFile?.let { mutableKSFileSourceMapper[it.filePath] = containingFile }
        navArgs?.file?.let { mutableKSFileSourceMapper[it.filePath] = it }

        return RawNavGraphGenParams(
            sourceIds = listOfNotNull(containingFile?.filePath, navArgs?.file?.filePath),
            routeOverride = if (navGraphAnnotationNameArg == NAV_GRAPH_ANNOTATION_DEFAULT_NAME) null else navGraphAnnotationNameArg,
            default = navGraphAnnotationDefaultArg,
            isNavHostGraph = isNavHostGraph,
            defaultTransitions = navGraphDefaultTransitions,
            annotationType = Importable(
                this.simpleName.asString(),
                this.qualifiedName!!.asString()
            ),
            parent = parent,
            isParentStart = isParentStart,
            deepLinks = deepLinks,
            navArgs = navArgs?.type,
            visibility = navGraphVisibility,
            externalStartRoute = externalStartRoute,
            externalNavGraphs = externalNavGraphs,
            externalDestinations = externalDestinations,
        )
    }

    private fun getExternalDestinations(externalRoutesAnnotation: KSAnnotation?): List<ExternalRoute> {
        return externalRoutesAnnotation?.findArgumentValue<ArrayList<KSType>>("destinations")?.map {
            ExternalRoute(
                generatedType = Importable(
                    it.declaration.simpleName.asString(),
                    it.declaration.qualifiedName!!.asString()
                ),
                // normal external destinations don't need this info, we can not populate it
                // we will do that though for the external start route if that exists
                navArgs = null,
                isDestination = true,
                requireOptInAnnotationTypes = it.declaration.findAllRequireOptInAnnotations()
            )
        }.orEmpty()
    }

    private fun KSClassDeclaration.getExternalStartRoute(
        externalRoutes: KSAnnotation?,
        externalNavGraphs: List<ExternalRoute>,
        externalDestinations: List<ExternalRoute>
    ): ExternalRoute? {
        return externalRoutes?.findArgumentValue<KSType>("startRoute")?.let { startRoute ->
            if ((startRoute.declaration as KSClassDeclaration).isNothing) {
                return null
            }

            val matchingNavGraph =
                externalNavGraphs.firstOrNull { it.generatedType.qualifiedName == startRoute.declaration.qualifiedName!!.asString() }
            if (matchingNavGraph != null) {
                return matchingNavGraph
            }

            val generatedType = Importable(
                simpleName = startRoute.declaration.simpleName.asString(),
                qualifiedName = startRoute.declaration.qualifiedName!!.asString()
            )

            if (externalDestinations.none { it.generatedType.qualifiedName == generatedType.qualifiedName }) {
                throw IllegalDestinationsSetup("`startRoute` of ${this.simpleName.asString()} is not present in the `destinations` or `navGraphs` list provided! " +
                        "External start route must be one of the external destination or nav graphs.")
            }

            val superType =
                (startRoute.declaration as KSClassDeclaration).superTypes.first().resolve()
            val navArgs =
                if (superType.declaration.simpleName.asString() == "TypedDestinationSpec") {
                    superType.arguments.first().type!!.resolve()
                        .getNavArgsDelegateType(resolver, navTypeSerializersByType)?.type
                } else {
                    null
                }

            ExternalRoute(
                generatedType = generatedType,
                navArgs = navArgs,
                isDestination = true,
                startRoute.declaration.findAllRequireOptInAnnotations()
            )
        }
    }

    private fun getExternalNavGraphs(externalRoutes: KSAnnotation?): List<ExternalRoute> {
        return externalRoutes
            ?.findArgumentValue<ArrayList<KSType>>("nestedNavGraphs")
            ?.map { graphType ->
                val superType =
                    (graphType.declaration as KSClassDeclaration).superTypes.first().resolve()

                val navArgs =
                    if (superType.declaration.simpleName.asString() == "TypedNavGraphSpec") {
                        superType.arguments.first().type!!.resolve()
                            .getNavArgsDelegateType(resolver, navTypeSerializersByType)?.type
                    } else {
                        null
                    }

                ExternalRoute(
                    generatedType = Importable(
                        simpleName = graphType.declaration.simpleName.asString(),
                        qualifiedName = graphType.declaration.qualifiedName!!.asString()
                    ),
                    navArgs = navArgs,
                    isDestination = false,
                    graphType.declaration.findAllRequireOptInAnnotations()
                )
            }.orEmpty()
    }
}
