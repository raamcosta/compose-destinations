package com.ramcosta.samples.destinationstodosample.ui.screens.greeting

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GreetingViewModel: ViewModel(), GreetingUiEvents, GreetingUiState {

    private val greetings = arrayOf(
        "Hello",
        "What's up",
        "Howdy",
        "Hi there!",
        "Hi folks!",
        "Long time, no see",
        "Hey",
        "How are you"
    )

    private var currentGreetingIndex = 0

    override var greeting: String by mutableStateOf(greetings[currentGreetingIndex])

    init {
        Log.d("GreetingViewModel", "initing---$this")
    }

    override fun onNewGreetingClicked() {
        Log.d("GreetingViewModel", "onCounterButtonClicked")
        if (currentGreetingIndex == greetings.lastIndex) {
            currentGreetingIndex = 0
        } else {
            currentGreetingIndex++
        }

        greeting = greetings[currentGreetingIndex]
    }

    override fun onCleared() {
        Log.d("GreetingViewModel", "onCleared---$this")
    }

}