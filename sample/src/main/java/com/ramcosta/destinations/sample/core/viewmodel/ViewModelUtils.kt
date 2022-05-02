package com.ramcosta.destinations.sample.core.viewmodel

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.savedstate.SavedStateRegistryOwner
import com.ramcosta.destinations.sample.LocalDependencyContainer
import com.ramcosta.destinations.sample.MainViewModel
import com.ramcosta.destinations.sample.account.AccountViewModel
import com.ramcosta.destinations.sample.core.di.DependencyContainer

@Composable
inline fun <reified VM : ViewModel> viewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    savedStateRegistryOwner: SavedStateRegistryOwner = LocalSavedStateRegistryOwner.current
): VM {
    return androidx.lifecycle.viewmodel.compose.viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = ViewModelFactory(
            owner = savedStateRegistryOwner,
            defaultArgs = (savedStateRegistryOwner as? NavBackStackEntry)?.arguments,
            dependencyContainer = LocalDependencyContainer.current,
        )
    )
}

@Composable
inline fun <reified VM : ViewModel> activityViewModel(): VM {
    val activity = LocalDependencyContainer.current.activity

    return androidx.lifecycle.viewmodel.compose.viewModel(
        VM::class.java,
        activity,
        null,
        factory = ViewModelFactory(
            owner = activity,
            defaultArgs = null,
            dependencyContainer = LocalDependencyContainer.current,
        )
    )
}

class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle?,
    //remove this line if you're not using Dependency injection
    private val dependencyContainer: DependencyContainer
) : AbstractSavedStateViewModelFactory(
    owner,
    defaultArgs
) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return when (modelClass) {
            MainViewModel::class.java -> MainViewModel(dependencyContainer.loginStateRepository)
            AccountViewModel::class.java -> AccountViewModel(dependencyContainer.loginStateRepository)
            else -> throw RuntimeException("Unknown view model $modelClass")
        } as T
    }
}