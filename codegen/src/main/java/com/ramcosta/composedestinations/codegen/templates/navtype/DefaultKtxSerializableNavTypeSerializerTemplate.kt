package com.ramcosta.composedestinations.codegen.templates.navtype

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

val defaultKtxSerializableNavTypeSerializerTemplatePkg =
    "$codeGenBasePackageName.navargs.ktxserializable"
const val DEFAULT_KTX_SERIALIZABLE_NAV_TYPE_SERIALIZER_TEMPLATE_NAME =
    "DefaultKtxSerializableNavTypeSerializer"

val defaultKtxSerializableNavTypeSerializerTemplate = FileTemplate(
    packageStatement = "package $defaultKtxSerializableNavTypeSerializerTemplatePkg",
    imports = setOfImportable(
        "$CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer",
        "kotlinx.serialization.KSerializer",
        "kotlinx.serialization.Serializable",
        "kotlinx.serialization.json.Json",
    ),
    sourceCode = """
/**
 * Default [DestinationsNavTypeSerializer] for Kotlin [Serializable]s.
 *
 * This gets used by the generated code if you don't provide an explicit
 * [DestinationsNavTypeSerializer] annotated with `@NavTypeSerializer` for the type being
 * passed as navigation argument.
 */
public class $DEFAULT_KTX_SERIALIZABLE_NAV_TYPE_SERIALIZER_TEMPLATE_NAME<T : @Serializable Any>(
    private val serializer: KSerializer<T>,
) : DestinationsNavTypeSerializer<T> {

    override fun toRouteString(value: T): String {
        return Json.encodeToString(serializer, value)
    }

    override fun fromRouteString(routeStr: String): T {
        return Json.decodeFromString(serializer, routeStr)
    }
}

""".trimIndent()
)

//TODO RACOSTA keep same behaviour for android by serializing to a base64 str?
