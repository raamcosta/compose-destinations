package com.ramcosta.samples.destinationstodosample.di

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.savedstate.SavedStateRegistryOwner
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.samples.destinationstodosample.LocalDependencyContainer
import com.ramcosta.samples.destinationstodosample.ui.screens.destinations.ProfileScreenDestination
import com.ramcosta.samples.destinationstodosample.ui.screens.greeting.GreetingViewModel
import com.ramcosta.samples.destinationstodosample.ui.screens.profile.ProfileViewModel
import com.ramcosta.samples.destinationstodosample.ui.screens.settings.SettingsViewModel

@Composable
inline fun <reified VM : ViewModel> DependenciesContainerBuilder<*>.viewModel(navGraphSpec: NavGraphSpec): VM {
    val parentEntry = remember { navController.getBackStackEntry(navGraphSpec.route) }

    return viewModel(parentEntry, parentEntry)
}

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

class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle?,
    private val dependencyContainer: DependencyContainer
) : AbstractSavedStateViewModelFactory(
    owner,
    defaultArgs
) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return when (modelClass) {
            ProfileViewModel::class.java -> ProfileViewModel(
                dependencyContainer.getProfileLikeCount,
                ProfileScreenDestination.argsFrom(handle)
            )

            GreetingViewModel::class.java -> GreetingViewModel()

            SettingsViewModel::class.java -> SettingsViewModel()

            else -> throw RuntimeException("Unknown view model $modelClass")
        } as T
    }
}