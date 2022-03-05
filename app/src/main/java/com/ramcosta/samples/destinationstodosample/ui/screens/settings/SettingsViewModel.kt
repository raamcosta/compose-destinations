package com.ramcosta.samples.destinationstodosample.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel() {

    private var _isToggleOn = mutableStateOf(false)
    val isToggleOn: Boolean by _isToggleOn

    fun toggle() {
        _isToggleOn.value = !isToggleOn
    }
}