package com.ramcosta.samples.destinationstodosample.vms

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

interface GreetingUiEvents {
    fun onCounterButtonClicked()
}

interface GreetingUiState {

    val counter: Int
}

class GreetingViewModel: ViewModel(), GreetingUiEvents, GreetingUiState {

    override var counter: Int by mutableStateOf(0)

    init {
        Log.d("GreetingViewModel", "initing---$this")
    }

    override fun onCounterButtonClicked() {
        Log.d("GreetingViewModel", "onCounterButtonClicked")
        counter++
    }

    override fun onCleared() {
        Log.d("GreetingViewModel", "onCleared---$this")
    }

}