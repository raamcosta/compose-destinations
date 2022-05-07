package com.ramcosta.destinations.sample.core.di

import androidx.lifecycle.SavedStateHandle
import com.ramcosta.destinations.sample.MainActivity
import com.ramcosta.destinations.sample.MainViewModel
import com.ramcosta.destinations.sample.account.AccountViewModel
import com.ramcosta.destinations.sample.login.data.LoginStateRepository
import com.ramcosta.destinations.sample.tasks.data.StepsRepository
import com.ramcosta.destinations.sample.tasks.data.TasksRepository
import com.ramcosta.destinations.sample.tasks.presentation.details.TaskDetailsViewModel
import com.ramcosta.destinations.sample.tasks.presentation.list.TaskListViewModel
import com.ramcosta.destinations.sample.tasks.presentation.newtask.AddTaskViewModel

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
            TaskListViewModel::class.java -> TaskListViewModel(tasksRepository)
            AddTaskViewModel::class.java -> AddTaskViewModel(tasksRepository)
            TaskDetailsViewModel::class.java -> TaskDetailsViewModel(
                handle,
                tasksRepository,
                stepsRepository
            )
            else -> throw RuntimeException("Unknown view model $modelClass")
        } as T
    }
}