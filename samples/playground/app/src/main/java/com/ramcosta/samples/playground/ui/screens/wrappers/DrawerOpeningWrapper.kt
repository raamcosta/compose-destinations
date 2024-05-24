package com.ramcosta.samples.playground.ui.screens.wrappers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ramcosta.composedestinations.navigation.require
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import com.ramcosta.samples.playground.commons.DrawerController

object DrawerOpeningWrapper : DestinationWrapper {

    @Composable
    override fun <T> DestinationScope<T>.Wrap(
        screenContent: @Composable () -> Unit
    ) {
        val dependencies = buildDependencies()
        val drawerController = dependencies.require<DrawerController>()

        LaunchedEffect(Unit) {
            drawerController.open()
            drawerController.close()
            drawerController.open()
            drawerController.close()
        }

        screenContent()
    }

}
