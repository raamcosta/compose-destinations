package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.CORE_BOTTOM_SHEET_DESTINATION_STYLE
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper

sealed class DestinationStyleType {

    abstract fun code(importableHelper: ImportableHelper): String

    data class Animated(val importable: Importable, val requireOptInAnnotations: List<Importable>) : DestinationStyleType() {
        override fun code(importableHelper: ImportableHelper): String {
            return importableHelper.addAndGetPlaceholder(importable)
        }
    }

    data class Dialog(val importable: Importable) : DestinationStyleType() {
        override fun code(importableHelper: ImportableHelper): String {
            return importableHelper.addAndGetPlaceholder(importable)
        }
    }

    data object BottomSheet : DestinationStyleType() {
        override fun code(importableHelper: ImportableHelper): String {
            val bottomSheetImportable = Importable(
                CORE_BOTTOM_SHEET_DESTINATION_STYLE,
                "$CORE_PACKAGE_NAME.bottomsheet.spec.$CORE_BOTTOM_SHEET_DESTINATION_STYLE",
            )

            return importableHelper.addAndGetPlaceholder(bottomSheetImportable)
        }
    }

    data object Default : DestinationStyleType() {
        override fun code(importableHelper: ImportableHelper): String {
            val bottomSheetImportable = Importable(
                "Default",
                "$CORE_PACKAGE_NAME.spec.DestinationStyle.Default",
            )

            return importableHelper.addAndGetPlaceholder(bottomSheetImportable)
        }
    }

    data object Activity: DestinationStyleType() {
        override fun code(importableHelper: ImportableHelper): String {
            error("Activity DestinationStyle is internal! Cannot be used!")
        }
    }
}