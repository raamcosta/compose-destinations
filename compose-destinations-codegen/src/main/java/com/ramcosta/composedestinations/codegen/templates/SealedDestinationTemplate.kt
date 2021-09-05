package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.DESTINATION_SPEC

val sealedDestinationTemplate = """
package com.ramcosta.composedestinations

/**
 * When using the code gen module, all APIs will expose
 * $DESTINATION_SPEC which is a sealed version of [Destination]
 */
sealed interface $DESTINATION_SPEC : Destination

""".trimIndent()