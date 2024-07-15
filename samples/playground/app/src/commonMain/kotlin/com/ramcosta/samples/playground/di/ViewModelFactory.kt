package com.ramcosta.samples.playground.di

//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.core.bundle.Bundle
//import androidx.lifecycle.AbstractSavedStateViewModelFactory
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelStoreOwner
//import androidx.lifecycle.viewmodel.CreationExtras
//import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
//import androidx.navigation.NavBackStackEntry
//import androidx.savedstate.SavedStateRegistryOwner
//import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
//import com.ramcosta.composedestinations.spec.NavGraphSpec
//import com.ramcosta.samples.playground.LocalDIContainer
//import com.ramcosta.samples.playground.ui.screens.greeting.GreetingViewModel
//import com.ramcosta.samples.playground.ui.screens.profile.ProfileViewModel
//import com.ramcosta.samples.playground.ui.screens.settings.SettingsViewModel
//import com.ramcosta.samples.playground.ui.screens.wrappers.HidingScreenWrapperViewModel
//import kotlin.reflect.KClass

//@Composable
//inline fun <reified VM : ViewModel> DependenciesContainerBuilder<*>.viewModel(navGraphSpec: NavGraphSpec): VM {
//    val parentEntry = remember(navBackStackEntry) {
//        navController.getBackStackEntry(navGraphSpec.route)
//    }
//
//    return viewModel(parentEntry)
//}
//
//@Composable
//inline fun <reified VM : ViewModel> viewModel(
//    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
//        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
//    },
//): VM {
//    return androidx.lifecycle.viewmodel.compose.viewModel(
//        viewModelStoreOwner = viewModelStoreOwner,
//        factory = ViewModelFactory(
//            owner = viewModelStoreOwner as SavedStateRegistryOwner,
//            defaultArgs = (viewModelStoreOwner as? NavBackStackEntry)?.arguments,
//            dependencyContainer = LocalDIContainer.current,
//        )
//    )
//}
//
//class ViewModelFactory(
//    private val dependencyContainer: DependencyContainer,
//    owner: SavedStateRegistryOwner,
//    defaultArgs: Bundle? = null,
//) : AbstractSavedStateViewModelFactory(
//    owner,
//    defaultArgs
//) {
//    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
//        return super.create(modelClass, extras)
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(
//        key: String,
//        modelClass: KClass<T>,
//        handle: SavedStateHandle
//    ): T {
//        return when (modelClass) {
//            ProfileViewModel::class -> ProfileViewModel(
//                dependencyContainer.getProfileLikeCount,
//                handle
//            )
//
//            GreetingViewModel::class -> GreetingViewModel()
//
//            SettingsViewModel::class -> SettingsViewModel()
//
//            HidingScreenWrapperViewModel::class -> HidingScreenWrapperViewModel()
//
//            else -> throw RuntimeException("Unknown view model $modelClass")
//        } as T
//    }
//}