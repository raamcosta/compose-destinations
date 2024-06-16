@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.ramcosta.composedestinations.annotation

import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalObjCRefinement::class)
actual typealias PlatformHiddenFromObjC = HiddenFromObjC

actual annotation class PlatformKeep()