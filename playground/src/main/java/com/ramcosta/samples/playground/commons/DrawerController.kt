package com.ramcosta.samples.playground.commons

import androidx.compose.material.DrawerState

interface DrawerController {
    suspend fun open()
    suspend fun close()
}

class DrawerControllerImpl(
    private val drawerState: DrawerState
): DrawerController{

    override suspend fun open() {
        drawerState.open()
    }

    override suspend fun close() {
        drawerState.close()
    }
}