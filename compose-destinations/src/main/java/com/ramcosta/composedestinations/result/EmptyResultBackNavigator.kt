package com.ramcosta.composedestinations.result

import kotlin.reflect.KType

/**
 * Empty implementation of [ResultBackNavigator] to
 * use in previews and possibly testing.
 */
class EmptyResultBackNavigator<R> : ResultBackNavigator<R> {

    override fun navigateBack(result: R, type: KType?) = Unit

    override fun setResult(result: R, type: KType?) = Unit

    override fun navigateBack() = Unit
}