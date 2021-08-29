package com.ramcosta.composedestinations.commons

import com.ramcosta.composedestinations.codegen.model.DefaultValue
import com.ramcosta.composedestinations.codegen.model.Known
import org.junit.Test

class DefaultParameterValueReaderTest {

    private val reader = DefaultParameterValueReader()

    private val casesToTest = arrayOf(
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\") {",
            argName = "arg1",
            argType = "String",
            expected = Known("\"defaultArg\"")
        ),
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\"",
            argName = "arg1",
            argType = "String",
            expected = Known("\"defaultArg\"")
        ),
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\",",
            argName = "arg1",
            argType = "String",
            expected = Known("\"defaultArg\"")
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