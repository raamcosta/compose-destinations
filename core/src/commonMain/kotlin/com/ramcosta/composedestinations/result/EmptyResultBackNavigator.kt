package com.ramcosta.composedestinations.result

/**
 * Empty implementation of [ResultBackNavigator] to
 * use in previews and possibly testing.
 */
class EmptyResultBackNavigator<R> : ResultBackNavigator<R> {

    override fun navigateBack(result: R) = Unit

    override fun setResult(result: R) = Unit

    override fun navigateBack() = Unit
}