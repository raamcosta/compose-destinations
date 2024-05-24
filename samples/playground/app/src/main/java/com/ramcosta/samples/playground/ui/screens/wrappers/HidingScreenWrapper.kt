package com.ramcosta.samples.playground.ui.screens.wrappers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import com.ramcosta.samples.playground.di.activityViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object HidingScreenWrapper : DestinationWrapper {

    @Composable
    override fun <T> DestinationScope<T>.Wrap(
        screenContent: @Composable () -> Unit
    ) {
        val vm = activityViewModel<HidingScreenWrapperViewModel>()
        val showingScreen by vm.showingScreen.collectAsState()

        if (showingScreen) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hiding the screen in ${vm.timeLeft.collectAsState().value}")
                screenContent()
            }
        } else {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { vm.onShowButtonClick() }
                ) {
                    Text("CLICK ME TO SHOW SCREEN!")
                }
            }
        }
    }
}

class HidingScreenWrapperViewModel : ViewModel() {

    private val _showingScreen = MutableStateFlow(false)
    val showingScreen = _showingScreen.asStateFlow()

    private val _timeLeft = MutableStateFlow<String?>(null)
    val timeLeft = _timeLeft.asStateFlow()

    fun onShowButtonClick() {
        _showingScreen.update { true }
        viewModelScope.launch {
            var secondsLeft = 5
            while (secondsLeft > 0) {
                _timeLeft.update { "$secondsLeft seconds" }
                secondsLeft--
                delay(1000)
            }
            _showingScreen.update { false }
        }
    }
}
