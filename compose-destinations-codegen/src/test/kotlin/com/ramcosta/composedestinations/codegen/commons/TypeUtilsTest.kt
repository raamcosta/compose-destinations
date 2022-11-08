package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import `is`.it.kotlin.keywords.KeywordPackageClass
import org.junit.Test

class TypeUtilsTest {

    @Test
    fun `escape kotlin keywords in Importable`() {
        val keywordPackageClassTypeInfo = TypeInfo(
            value = KeywordPackageClass::class.asType(),
            isNullable = false,
            hasCustomTypeSerializer = false,
        )
        assert(
            keywordPackageClassTypeInfo
                .value
                .importable
                .qualifiedName
                .split(".")
                .any { keywords.contains(it) }
                .not()
        ) {
            "Generated Importable '${keywordPackageClassTypeInfo.importable}' contains unescaped Kotlin keywords"
        }
    }

    @Test
    fun `escape kotlin keywords in TypeCode`() {
        val importableHelper = ImportableHelper()
        val keywordPackageClassTypeInfo = TypeInfo(
            value = KeywordPackageClass::class.asType(),
            isNullable = false,
            hasCustomTypeSerializer = false,
        )

        val typeCode = keywordPackageClassTypeInfo.toTypeCode(importableHelper)
        assert(typeCode.split(".").any { keywords.contains(it) }.not()) {
            "Generated TypeCode '$typeCode' contains unescaped Kotlin keywords"
        }
    }

    @Test
    fun `escape kotlin keywords in TypeCode without helper`() {
        val keywordPackageClassTypeInfo = TypeInfo(
            value = KeywordPackageClass::class.asType(),
            isNullable = false,
            hasCustomTypeSerializer = false,
        )

        val typeCode = keywordPackageClassTypeInfo.toTypeCode()
        assert(typeCode.split(".").any { keywords.contains(it) }.not()) {
            "Generated TypeCode '$typeCode' contains unescaped Kotlin keywords"
        }
    }
}
