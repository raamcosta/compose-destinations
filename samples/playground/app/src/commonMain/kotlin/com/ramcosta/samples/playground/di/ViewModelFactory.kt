package com.ramcosta.samples.playground.di

import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.core.bundle.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.savedstate.SavedStateRegistryOwner
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.samples.playground.LocalDIContainer
import com.ramcosta.samples.playground.ui.screens.greeting.GreetingViewModel
import com.ramcosta.samples.playground.ui.screens.profile.ProfileViewModel
import com.ramcosta.samples.playground.ui.screens.settings.SettingsViewModel
import com.ramcosta.samples.playground.ui.screens.wrappers.HidingScreenWrapperViewModel

@Composable
inline fun <reified VM : ViewModel> DependenciesContainerBuilder<*>.viewModel(navGraphSpec: NavGraphSpec): VM {
    val parentEntry = remember(navBackStackEntry) {
        navController.getBackStackEntry(navGraphSpec.route)
    }

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
            dependencyContainer = LocalDIContainer.current,
        )
    )
}

@Composable
inline fun <reified VM : ViewModel> activityViewModel(): VM {
    val activity = LocalActivity
    return ViewModelProvider(
        owner = activity,
        factory = ViewModelFactory(
            LocalDIContainer.current,
            activity
        )
    )[VM::class.java]
}

class ViewModelFactory(
    private val dependencyContainer: DependencyContainer,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null,
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
            ProfileViewModel::class.java -> ProfileViewModel(
                dependencyContainer.getProfileLikeCount,
                handle
            )

            GreetingViewModel::class.java -> GreetingViewModel()

            SettingsViewModel::class.java -> SettingsViewModel()

            HidingScreenWrapperViewModel::class.java -> HidingScreenWrapperViewModel()

            else -> throw RuntimeException("Unknown view model $modelClass")
        } as T
    }
}

val LocalActivity: ComponentActivity
    @Composable
    get() {
        return LocalContext.current.let {
            var ctx = it
            while (ctx is ContextWrapper) {
                if (ctx is ComponentActivity) {
                    return@let ctx
                }
                ctx = ctx.baseContext
            }

            error("Expected an activity context but instead found: $ctx")
        }
    }