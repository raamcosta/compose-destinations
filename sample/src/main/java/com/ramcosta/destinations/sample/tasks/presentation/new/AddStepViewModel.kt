package com.ramcosta.destinations.sample.tasks.presentation.new

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.destinations.sample.tasks.data.StepsRepository
import com.ramcosta.destinations.sample.tasks.domain.Step
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddStepViewModel(
    private val taskId: Int,
    private val stepsRepository: StepsRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _title.update { newTitle }
    }

    fun onConfirmClicked() {
        viewModelScope.launch {
            stepsRepository.addNewStep(Step(taskId, _title.value, false))
        }
    }
}
