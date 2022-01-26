package com.ramcosta.composedestinations.result

class EmptyResultBackNavigator<R> : ResultBackNavigator<R> {
    override fun navigateBack(result: R) = Unit
}