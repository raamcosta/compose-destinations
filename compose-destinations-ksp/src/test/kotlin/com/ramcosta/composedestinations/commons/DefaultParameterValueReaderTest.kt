package com.ramcosta.composedestinations.commons

import com.ramcosta.composedestinations.codegen.model.DefaultValue
import org.junit.Test

class DefaultParameterValueReaderTest {

    private val reader = DefaultParameterValueReader()

    private val casesToTest = arrayOf(
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\") {",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue.Known("\"defaultArg\"")
        ),
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\"",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue.Known("\"defaultArg\"")
        ),
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\",",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue.Known("\"defaultArg\"")
        )
    )

    @Test
    fun testCases() {
        casesToTest.forEach {
            val result = reader.readDefaultValue(
                it.lineText,
                it.argName,
                it.argType
            )

            assert(result == it.expected)
        }
    }

    class TestCase(
        val lineText: String,
        val argName: String,
        val argType: String,
        val expected: DefaultValue
    )
}