package com.ramcosta.composedestinations

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

private const val DESTINATION_ANNOTATION = "Screen"
//private const val DESTINATION_ANNOTATION_ARGUMENT = "destination"
private const val DESTINATION_ANNOTATION_ROUTE_ARGUMENT = "route"
//private const val DESTINATION_ANNOTATION_ARG_TYPES_ARGUMENT = "argTypes"
//private const val DESTINATION_ANNOTATION_ARGUMENTS_ARGUMENT = "arguments"

private const val DESTINATION_DEFINITION = "ScreenDestination"
private const val DESTINATION_DEFINITION_SUFFIX = "Destination"

private const val DESTINATIONS_AGGREGATE_CLASS = "Screens"

class Processor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols: MutableList<KSFunctionDeclaration> =
            resolver.getSymbolsWithAnnotation("$packageName.$DESTINATION_ANNOTATION")
                .filterIsInstance<KSFunctionDeclaration>().toMutableList()

        if (!symbols.iterator().hasNext()) return emptyList()

        val qualifiedNames = mutableListOf<String>()
        val simpleNames = mutableListOf<String>()

//        symbols.retainAll { symbol ->
//            val destination = symbol
//                .findAnnotation(DESTINATION_ANNOTATION)
//                .findArgumentValue<KSType>(DESTINATION_ANNOTATION_ARGUMENT)?.declaration
//
//            if (destination != null && destination.qualifiedName!!.asString() != "$packageName.$DESTINATION_DEFINITION") {
//                qualifiedNames.add(destination.qualifiedName!!.asString())
//                simpleNames.add(destination.simpleName.asString())
//
//                false
//            } else {
//                true
//            }
//        }

        symbols.forEach { symbol ->
            val fileName = symbol.simpleName.asString() + DESTINATION_DEFINITION_SUFFIX
            qualifiedNames.add("$packageName.$fileName")
            simpleNames.add(fileName)


            val outputStream = codeGenerator.createNewFile(
                dependencies = Dependencies.ALL_FILES,
                packageName = packageName,
                fileName = fileName
            )

//            val screenAnnotation = symbol.findAnnotation(DESTINATION_ANNOTATION)
//            val argTypes = screenAnnotation.findArgumentValue<ArrayList<KSType>>(DESTINATION_ANNOTATION_ARG_TYPES_ARGUMENT)
//            val arguments = screenAnnotation.findArgumentValue<ArrayList<String>>(DESTINATION_ANNOTATION_ARGUMENTS_ARGUMENT)
            val argTypes = symbol.parameters.map { it.type.resolve() }

            outputStream += """
                package $packageName

                import androidx.compose.runtime.Composable
                import androidx.navigation.NavBackStackEntry
                import androidx.navigation.NavController
                import androidx.navigation.NavType
                import androidx.navigation.compose.navArgument
                import ${symbol.qualifiedName?.asString()}

                object $fileName: ScreenDestination {

                    override val route get() = "${symbol.findAnnotation(DESTINATION_ANNOTATION).findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)}"
                    %s0
                    @Composable
                    override fun Content(
                        navController: NavController,
                        navBackStackEntry: NavBackStackEntry
                    ) {
                        ${prepareDefaultArgs(symbol.parameters, argTypes)}
                        ${callActualComposable(symbol, argTypes)}
                    }
                }
            """.trimIndent()
                .replace("%s0", navArgumentsDeclarationCode(symbol.parameters, argTypes))
        }

        val file: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = packageName,
            fileName = DESTINATIONS_AGGREGATE_CLASS
        )

        file += screensTemplate
            .replace("%s0", importsCode(qualifiedNames))
            .replace("%s1", qualifiedNames.size.toString())
            .replace("%s2", allScreensInsideArrayCode(simpleNames))


        file.close()
        return symbols.filterNot { it.validate() }.toList()
    }

    private fun callActualComposable(
        symbol: KSFunctionDeclaration,
        argTypes: List<KSType>
    ): String {

//        val finalParams = symbol.parameters.toMutableList()
//        val finalArgTypes = argTypes.toMutableList()

        var callingCode = ""

        symbol.parameters.forEachIndexed { i, it ->
            if (it.hasDefault) {
                callingCode += """
                        if (!contains_${it.name!!.asString()}) {
                            ${symbol.simpleName.asString()}(${prepareArguments(symbol.parameters.toMutableList().apply { removeAt(i) }, argTypes.toMutableList().apply { removeAt(i) })})         
                        } else """
            }
        }

        callingCode += "${symbol.simpleName.asString()}(${prepareArguments(symbol.parameters, argTypes)})"
        return callingCode
    }

    private fun prepareDefaultArgs(
        parameters: List<KSValueParameter>,
        argTypes: List<KSType>
    ): String {
        var defaultArgsPrep = ""
        parameters.forEachIndexed { i, it ->
            if (!it.hasDefault) return@forEachIndexed

            val name = it.name?.asString()!!
            defaultArgsPrep += "val contains_$name = ${backStackEntryContains(name)}"
        }

        return defaultArgsPrep
    }

    private fun backStackEntryContains(name: String): String {
        return "navBackStackEntry.arguments?.containsKey(\"$name\") ?: false"
    }

    private fun prepareArguments(parameters: List<KSValueParameter>, argTypes: List<KSType>): String {
        var argsCode = ""

        argTypes.forEachIndexed { i, it ->
            val name = parameters[i].name!!.asString()

            if (i != 0) argsCode += ", "

            argsCode += "$name = ${resolveArgumentForTypeAndName(it, name)}"
        }

        return argsCode
    }

    private fun resolveArgumentForTypeAndName(type: KSType, name: String): String {
        return when (type.declaration.qualifiedName!!.asString()) {
            "androidx.navigation.NavController" -> "navController" //part of the normal Compose arguments, so just return it
            else -> "navBackStackEntry.arguments?.${type.toNavBackStackEntryArgGetter(name)}!!"
        }
    }

    private fun navArgumentsDeclarationCode(arguments: List<KSValueParameter>, argTypes: List<KSType>): String {
        val code = StringBuilder()

        val finalArgs = mutableListOf<KSValueParameter>()
        val finalArgTypes = argTypes.filterIndexed { i, it ->
            if (it.declaration.qualifiedName?.asString() == "androidx.navigation.NavController") {
                return@filterIndexed false
            }

            finalArgs.add(arguments[i])
            return@filterIndexed true
        }

        finalArgs.forEachIndexed { i, it ->
            if (i == 0) {
                code.append("\n\toverride val arguments = listOf(\n\t\t")
            }

            val argName = it.name?.asString() //TODO check nullable and default values

             code.append("navArgument(\"${argName}\") { type = ${finalArgTypes[i].toNavTypeCode()} }")

            if (i != finalArgs.lastIndex) {
                code.append(",\n\t\t")
            } else {
                code.append("\n\t)\n")
            }
        }

        return code.toString()
    }

    private fun KSType.toNavBackStackEntryArgGetter(argName: String): String {
        return when (declaration.qualifiedName?.asString()) {
            String::class.qualifiedName -> "getString(\"$argName\")"
            else -> throw RuntimeException("Unknown type ${this.declaration.qualifiedName?.asString()}")
        }
    }

    private fun KSType.toNavTypeCode(): String {
        return when (declaration.qualifiedName?.asString()) {
            String::class.qualifiedName -> "NavType.StringType"
            else -> throw RuntimeException("Unknown type ${this.declaration.qualifiedName?.asString()}")
        }
    }

    private fun KSFunctionDeclaration.findAnnotation(name: String): KSAnnotation {
        return annotations.find { it.shortName.asString() == name }!!
    }

    private inline fun <reified T> KSAnnotation.findArgumentValue(name: String): T? {
        val value = arguments.find { it.name?.asString() == name }!!.value
        logger.warn("$this -> $name = $value (class: ${value?.javaClass})")
        return value as? T
    }

    private fun importsCode(qualifiedNames: List<String>): String {
        val code = StringBuilder()
        qualifiedNames.forEachIndexed { i, it ->
            code.append("import $it")
            if (i != qualifiedNames.lastIndex)
                code.append("\n")
        }

        return code.toString()
    }

    private fun allScreensInsideArrayCode(simpleNames: List<String>): String {
        val code = StringBuilder()
        simpleNames.forEachIndexed { i, it ->
            code.append("$it.route to $it")

            if (i != simpleNames.lastIndex)
                code.append(",\n\t\t")
        }

        return code.toString()
    }

    operator fun OutputStream.plusAssign(str: String) {
        this.write(str.toByteArray())
    }
}

private const val packageName = "com.ramcosta.composedestinations"

private val screensTemplate = """
package $packageName

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
%s0

object Screens {

    val screenCount = %s1

    val allScreens: Map<String, ScreenDestination> = mapOf(
        %s2
    )

    @Composable
    fun NavHost(
        navController: NavHostController,
        startDestination: ScreenDestination,
        modifier: Modifier = Modifier,
        route: String? = null,
        builder: NavGraphBuilder.() -> Unit = {}
    ) {
        ScreensNavHost(
            allScreens.values,
            navController,
            startDestination,
            modifier,
            route,
            builder
        )
    }

    @Composable
    fun Scaffold(
        startDestination: ScreenDestination,
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
        scaffoldState: ScaffoldState = rememberScaffoldState(),
        topBar: (@Composable () -> Unit)? = null,
        bottomBar: @Composable () -> Unit = {},
        snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
        floatingActionButton: @Composable () -> Unit = {},
        floatingActionButtonPosition: FabPosition = FabPosition.End,
        isFloatingActionButtonDocked: Boolean = false,
        drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
        drawerGesturesEnabled: Boolean = true,
        drawerShape: Shape = MaterialTheme.shapes.large,
        drawerElevation: Dp = DrawerDefaults.Elevation,
        drawerBackgroundColor: Color = MaterialTheme.colors.surface,
        drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
        drawerScrimColor: Color = DrawerDefaults.scrimColor,
        backgroundColor: Color = MaterialTheme.colors.background,
        contentColor: Color = contentColorFor(backgroundColor),
    ) {
        ScreensScaffold(
            allScreens,
            startDestination,
            modifier,
            navController,
            scaffoldState,
            topBar,
            bottomBar,
            snackbarHost,
            floatingActionButton,
            floatingActionButtonPosition,
            isFloatingActionButtonDocked,
            drawerContent,
            drawerGesturesEnabled,
            drawerShape,
            drawerElevation,
            drawerBackgroundColor,
            drawerContentColor,
            drawerScrimColor,
            backgroundColor,
            contentColor
        )
    }
}
""".trimIndent()

class MyTestVisitor(private val logger: KSPLogger) : KSVisitor<Unit, String> {
    override fun visitValueArgument(
        valueArgument: KSValueArgument,
        data: Unit
    ): String {
        logger.warn("visitValueArgument")
        return ""
    }

    override fun visitAnnotated(annotated: KSAnnotated, data: Unit): String {
        logger.warn("visitAnnotated")
        return ""
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit): String {
        logger.warn("visitAnnotation")
        return ""
    }

    override fun visitCallableReference(
        reference: KSCallableReference,
        data: Unit
    ): String {
        logger.warn("visitCallableReference")
        return ""
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit
    ): String {
        logger.warn("visitClassDeclaration")
        return ""
    }

    override fun visitClassifierReference(
        reference: KSClassifierReference,
        data: Unit
    ): String {
        logger.warn("visitClassifierReference")
        return ""
    }

    override fun visitDeclaration(declaration: KSDeclaration, data: Unit): String {
        logger.warn("visitDeclaration")
        return ""
    }

    override fun visitDeclarationContainer(
        declarationContainer: KSDeclarationContainer,
        data: Unit
    ): String {
        logger.warn("visitDeclarationContainer")
        return ""
    }

    override fun visitDynamicReference(
        reference: KSDynamicReference,
        data: Unit
    ): String {
        logger.warn("visitDynamicReference")
        return ""
    }

    override fun visitFile(file: KSFile, data: Unit): String {
        logger.warn("visitFile")
        return ""
    }

    override fun visitFunctionDeclaration(
        function: KSFunctionDeclaration,
        data: Unit
    ): String {
        logger.warn("visitFunctionDeclaration")
        return ""
    }

    override fun visitModifierListOwner(
        modifierListOwner: KSModifierListOwner,
        data: Unit
    ): String {
        logger.warn("visitModifierListOwner")
        return ""
    }

    override fun visitNode(node: KSNode, data: Unit): String {
        logger.warn("visitNode")
        return ""
    }

    override fun visitParenthesizedReference(
        reference: KSParenthesizedReference,
        data: Unit
    ): String {
        logger.warn("visitParenthesizedReference")
        return ""
    }

    override fun visitPropertyAccessor(
        accessor: KSPropertyAccessor,
        data: Unit
    ): String {
        logger.warn("visitPropertyAccessor")
        return ""
    }

    override fun visitPropertyDeclaration(
        property: KSPropertyDeclaration,
        data: Unit
    ): String {
        logger.warn("visitPropertyDeclaration")
        return ""
    }

    override fun visitPropertyGetter(getter: KSPropertyGetter, data: Unit): String {
        logger.warn("visitPropertyGetter")
        return ""
    }

    override fun visitPropertySetter(setter: KSPropertySetter, data: Unit): String {
        logger.warn("visitPropertySetter")
        return ""
    }

    override fun visitReferenceElement(
        element: KSReferenceElement,
        data: Unit
    ): String {
        logger.warn("visitReferenceElement")
        return ""
    }

    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit): String {
        logger.warn("visitTypeAlias")
        return ""
    }

    override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit): String {
        logger.warn("visitTypeArgument")
        return ""
    }

    override fun visitTypeParameter(
        typeParameter: KSTypeParameter,
        data: Unit
    ): String {
        logger.warn("visitTypeParameter")
        return ""
    }

    override fun visitTypeReference(
        typeReference: KSTypeReference,
        data: Unit
    ): String {
        logger.warn("visitTypeReference")
        return ""
    }

    override fun visitValueParameter(
        valueParameter: KSValueParameter,
        data: Unit
    ): String {
        logger.warn("visitValueParameter= $valueParameter")
        return ""
    }

}
