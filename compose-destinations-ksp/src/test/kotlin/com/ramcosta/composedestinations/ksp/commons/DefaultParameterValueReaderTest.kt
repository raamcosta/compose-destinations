package com.ramcosta.composedestinations.ksp.commons

import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.facades.PPrinter
import com.ramcosta.composedestinations.codegen.model.DefaultValue
import org.junit.Test

class DefaultParameterValueReaderTest {

    private val objectUnderTest = DefaultParameterValueReader

    private val samePackageResolver: (String, String) -> ResolvedSymbol? = { pckg, _ ->
        if (pckg == "com.ramcosta.composedestinations.ksp.commons") {
            ResolvedSymbol(true)
        } else null
    }

    private val casesToTest = arrayOf(
        TestCase(
            srcCodeText = "    arg1: String? = \"defaultArg\") {",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"defaultArg\"")
        ),
        TestCase(
            srcCodeText = """
                myInfo: MyInfo? = MyInfo()) {
                    val keyboardController = LocalSoftwareKeyboardController.current
                    val focusManager = LocalFocusManager.current
                    val context = LocalContext.current
                    val view = LocalView.current    
                    
                    LaunchedEffect(Unit) {
                            vm.method(myThing = thing1.thing2)
                    }
            """,
            argName = "myInfo",
            argType = "MyInfo",
            expected = DefaultValue("MyInfo()")
        ),
        TestCase(
            srcCodeText = "    arg1: String? = null,",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("null")
        ),
        TestCase(
            srcCodeText = "internal fun EditRitual(nav: RespawnNavigator, ritualId: Uuid? = null) = AnalyticsScreen(\"EditRitual\") {",
            argName = "ritualId",
            argType = "Uuid",
            imports = listOf("com.ramcosta.package.Uuid"),
            expected = DefaultValue("null", listOf())
        ),
        TestCase(
            srcCodeText = "    arg1: String? = \"defaultArg\"",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"defaultArg\"")
        ),
        TestCase(
            srcCodeText = "    arg1: String? = \"defaultArg\",",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"defaultArg\"")
        ),
        TestCase(
            srcCodeText = "arg1: String = \"multiple words string\",",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"multiple words string\"")
        ),
        TestCase(
            srcCodeText = "arg1: String = \"multiple, words string\", arg2: String = \"doesn't matter\"",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"multiple, words string\"")
        ),
        TestCase(
            srcCodeText = """
                cena: Class<Any> = Any::class.java,
            """,
            argName = "cena",
            argType = "Class",
            expected = DefaultValue("Any::class.java")
        ),
        TestCase(
            srcCodeText = """
                val configuration: SearchConfiguration = SearchConfiguration(),
                val appliedFilters: AppliedSearchFilters = AppliedSearchFilters(),
            )
            """,
            argName = "configuration",
            argType = "SearchConfiguration",
            expected = DefaultValue("SearchConfiguration()")
        ),
        TestCase(
            srcCodeText = """
                val myProperty: Boolean = false
                // This is an awesome (usually) property.
            """,
            argName = "myProperty",
            argType = "Boolean",
            expected = DefaultValue("false")
        ),
        TestCase(
            srcCodeText = """
                val myProperty: Boolean = false
                /* This is an awesome (usually) 
                *
                * property. */
            """,
            argName = "myProperty",
            argType = "Boolean",
            expected = DefaultValue("false")
        ),
        TestCase(
            srcCodeText = """val appliedFilters: AppliedSearchFilters = AppliedSearchFilters(),)@Preview@Composableprivate fun SearchScreenPreview(@PreviewParameter(PoiListPreviewParameterProvider::class, limit = 1) poiList: ImmutableList,) {OcmPreview {SearchScreenContent(poiCallbacks = PoiCallbacks(null, Origin.Deals, LocalFocusManager.current),""",
            argName = "appliedFilters",
            argType = "AppliedSearchFilters",
            expected = DefaultValue("AppliedSearchFilters()")
        ),
        TestCase(
            srcCodeText = """
                val appliedFilters: AppliedSearchFilters = AppliedSearchFilters(),
            )

            @Preview
            @Composable
            private fun SearchScreenPreview(
                @PreviewParameter(PoiListPreviewParameterProvider
            """,
            argName = "appliedFilters",
            argType = "AppliedSearchFilters",
            expected = DefaultValue("AppliedSearchFilters()")
        ),
        TestCase(
            srcCodeText = """
                arg1: String = "multiple, words string",
                arg2: String = "doesn't matter"
            """,
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"multiple, words string\"")
        ),
        TestCase(
            srcCodeText = """
                thingsWithNavTypeSerializer: Things? = null,
                serializableExample: SerializableExample? = SerializableExample(),
            """,
            argName = "thingsWithNavTypeSerializer",
            argType = "Things",
            expected = DefaultValue("null")
        ),
        TestCase(
            srcCodeText = """
                stuff3: ArrayList<Color>? = arrayListOf(),
                stuff4: SerializableExampleWithNavTypeSerializer? = SerializableExampleWithNavTypeSerializer(),
            """,
            argName = "stuff3",
            argType = "ArrayList",
            imports = emptyList(),
            expected = DefaultValue("arrayListOf()")
        ),
        TestCase(
            srcCodeText = "arg1: String = \"multiple, \\\"words string\", arg2: String = \"doesn't matter\"",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"multiple, \\\"words string\"")
        ),
        TestCase(
            srcCodeText = "arg1: String = \"doesn't matter\", arg2: String = \"mul\\\"tiple words \\\"string\"",
            argName = "arg2",
            argType = "String",
            expected = DefaultValue("\"mul\\\"tiple words \\\"string\"")
        ),
        TestCase(
            srcCodeText = "arg1: String = \"doesn't matter\", arg2: Int = 2, arg3: String = \"mul\\\"tiple words \\\"string\"",
            argName = "arg3",
            argType = "String",
            expected = DefaultValue("\"mul\\\"tiple words \\\"string\"")
        ),
        TestCase(
            srcCodeText = "arg1: String = \"doesn't matter\", arg2: Int = 2, arg3: String = \"mul\\\"tiple words \\\"string\"",
            argName = "arg2",
            argType = "Int",
            expected = DefaultValue("2")
        ),
        TestCase(
            srcCodeText = "arg1: Float = 123.0f",
            argName = "arg1",
            argType = "Float",
            expected = DefaultValue("123.0f")
        ),
        TestCase(
            srcCodeText = "arg1: Float = 123L",
            argName = "arg1",
            argType = "Float",
            expected = DefaultValue("123L")
        ),
        TestCase(
            srcCodeText = "arg1: Boolean = true",
            argName = "arg1",
            argType = "Boolean",
            expected = DefaultValue("true")
        ),


        // Same package public symbols


        TestCase(
            isResolvable = samePackageResolver,
            srcCodeText = "arg1: String = defaultValue()",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("defaultValue()", listOf("com.ramcosta.composedestinations.ksp.commons.defaultValue"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            srcCodeText = "arg1: String = Stuff.defaultValue()",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("Stuff.defaultValue()", listOf("com.ramcosta.composedestinations.ksp.commons.Stuff"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            srcCodeText = "arg1: String = Outer.Stuff.defaultValue()",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("Outer.Stuff.defaultValue()", listOf("com.ramcosta.composedestinations.ksp.commons.Outer"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            srcCodeText = "arg1: String = Stuff.defaultValue",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("Stuff.defaultValue", listOf("com.ramcosta.composedestinations.ksp.commons.Stuff"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            srcCodeText = "arg1: String = defaultValue",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("defaultValue", listOf("com.ramcosta.composedestinations.ksp.commons.defaultValue"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            srcCodeText = "arg1: String = Stuff.asd().stuff()",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("Stuff.asd().stuff()", listOf("com.ramcosta.composedestinations.ksp.commons.Stuff"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            srcCodeText = "arg1: String = Stuff.asd().stuff",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("Stuff.asd().stuff", listOf("com.ramcosta.composedestinations.ksp.commons.Stuff"))
        ),


        // other package public symbols (no .* imports)


        TestCase(
            srcCodeText = "arg1: String = defaultValue()",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.defaultValue"),
            expected = DefaultValue("defaultValue()", listOf("com.ramcosta.package.defaultValue"))
        ),
        TestCase(
            srcCodeText = "arg1: String = Outer.Stuff.defaultValue()",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.Outer"),
            expected = DefaultValue("Outer.Stuff.defaultValue()", listOf("com.ramcosta.package.Outer"))
        ),
        TestCase(
            srcCodeText = "arg1: String = defaultValue",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.defaultValue"),
            expected = DefaultValue("defaultValue", listOf("com.ramcosta.package.defaultValue"))
        ),
        TestCase(
            srcCodeText = "arg1: String = Stuff.asd().stuff()",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.Stuff"),
            expected = DefaultValue("Stuff.asd().stuff()", listOf("com.ramcosta.package.Stuff"))
        ),
        TestCase(
            srcCodeText = "arg1: String = Stuff.asd().stuff",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.Stuff"),
            expected = DefaultValue("Stuff.asd().stuff", listOf("com.ramcosta.package.Stuff"))
        ),
        TestCase(
            srcCodeText = """
                arg1: String = Stuff
                    .asd()
                    .stuff
            """,
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.Stuff"),
            expected = DefaultValue("Stuff.asd().stuff", listOf("com.ramcosta.package.Stuff"))
        ),
        TestCase(
            srcCodeText = """
                arg1: String = Stuff
                    .asd()
                    .stuff,
                arg2: String = someMethod(),
            )
            """,
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.Stuff"),
            expected = DefaultValue("Stuff.asd().stuff", listOf("com.ramcosta.package.Stuff"))
        ),
        TestCase(
            isResolvable = { pckg, _ -> if (pckg == "com.ramcosta.package") ResolvedSymbol(true) else null },
            srcCodeText = "arg1: String = Stuff.defaultValue",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.*"),
            expected = DefaultValue("Stuff.defaultValue", listOf("com.ramcosta.package.*"))
        ),
    )

    @Test
    fun testCases() {
        addLogger()
        casesToTest.forEachIndexed { idx, it ->
            println("Testing case #$idx: ${it.srcCodeText}")
            val result = objectUnderTest.readDefaultValue(
                resolver = it.isResolvable,
                srcCodeLines = it.srcCodeText.trimIndent().lines(),
                packageName = "com.ramcosta.composedestinations.ksp.commons",
                imports = it.imports,
                argName = it.argName,
                argType = it.argType
            )

            assert(result == it.expected) {
                "\nexpected= ${it.expected} \ngot= $result"
            }
        }
    }

    private fun addLogger() {
        Logger.instance = object : Logger {
            override val debugMode: Boolean
                get() = false
            override val debugModeOutputPath: String?
                get() = null
            override val prettyPrinter: PPrinter
                get() = kotlin.error("")

            override fun logging(message: String) {
                println("logging - $message")
            }

            override fun info(message: String) {
                println("info - $message")
            }

            override fun warn(message: String) {
                println("warn - $message")
            }

            override fun error(message: String) {
                println("error - $message")
            }

            override fun exception(e: Throwable) {
                println("exception - ${e.stackTraceToString()}")
            }

        }
    }

    class TestCase(
        val isResolvable: (String, String) -> ResolvedSymbol? = { _, _ -> null },
        val srcCodeText: String,
        val argName: String,
        val argType: String,
        val imports: List<String> = emptyList(),
        val expected: DefaultValue?
    )
}