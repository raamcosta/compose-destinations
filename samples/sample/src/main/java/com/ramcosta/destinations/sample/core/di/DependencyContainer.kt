package com.ramcosta.destinations.sample.core.di

import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.generated.navArgs
import com.ramcosta.destinations.sample.MainActivity
import com.ramcosta.destinations.sample.MainViewModel
import com.ramcosta.destinations.sample.account.AccountViewModel
import com.ramcosta.destinations.sample.login.data.LoginStateRepository
import com.ramcosta.destinations.sample.tasks.data.StepsRepository
import com.ramcosta.destinations.sample.tasks.data.TasksRepository
import com.ramcosta.destinations.sample.tasks.presentation.details.StepDetailsViewModel
import com.ramcosta.destinations.sample.tasks.presentation.details.StepScreenNavArgs
import com.ramcosta.destinations.sample.tasks.presentation.details.TaskDetailsViewModel
import com.ramcosta.destinations.sample.tasks.presentation.list.TaskListViewModel
import com.ramcosta.destinations.sample.tasks.presentation.new.AddStepDialogNavArgs
import com.ramcosta.destinations.sample.tasks.presentation.new.AddStepViewModel
import com.ramcosta.destinations.sample.tasks.presentation.new.AddTaskViewModel

class DependencyContainer(
    val activity: MainActivity
) {

    val loginStateRepository: LoginStateRepository by lazy { LoginStateRepository() }

    val tasksRepository: TasksRepository by lazy { TasksRepository(stepsRepository) }

    val stepsRepository: StepsRepository by lazy { StepsRepository() }

    @Suppress("UNCHECKED_CAST")
    fun <T> createViewModel(modelClass: Class<T>, handle: SavedStateHandle): T {
        return when (modelClass) {
            MainViewModel::class.java -> MainViewModel(loginStateRepository)
            AccountViewModel::class.java -> AccountViewModel(loginStateRepository)
            TaskListViewModel::class.java -> TaskListViewModel(tasksRepository, stepsRepository)
            AddTaskViewModel::class.java -> AddTaskViewModel(tasksRepository)
            AddStepViewModel::class.java -> AddStepViewModel(
                handle.navArgs<AddStepDialogNavArgs>().taskId,
                stepsRepository
            )
            TaskDetailsViewModel::class.java -> TaskDetailsViewModel(
                handle,
                tasksRepository,
                stepsRepository
            )
            StepDetailsViewModel::class.java -> StepDetailsViewModel(
                handle.navArgs<StepScreenNavArgs>().stepId,
                tasksRepository,
                stepsRepository
            )
            else -> throw RuntimeException("Unknown view model $modelClass")
        } as T
    }
}