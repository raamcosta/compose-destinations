package com.ramcosta.composedestinations.spec

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.annotation.InternalDestinationsApi
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.scope.DestinationScope

interface ActivityDestinationSpec<T> : DestinationSpec<T> {

    /**
     * See [androidx.navigation.ActivityNavigator.Destination.targetPackage]
     */
    val targetPackage: String? get() = null

    /**
     * See [androidx.navigation.ActivityNavigator.Destination.action]
     */
    val action: String? get() = null

    /**
     * See [androidx.navigation.ActivityNavigator.Destination.data]
     */
    val data: Uri? get() = null

    /**
     * See [androidx.navigation.ActivityNavigator.Destination.dataPattern]
     */
    val dataPattern: String? get() = null

    /**
     * Will add this class as the component name.
     * See [androidx.navigation.ActivityNavigator.Destination.setComponentName]
     */
    val activityClass: Class<out Activity>? get() = null

    /**
     * Returns the arguments [T] that were passed to this ActivityDestination by checking the
     * intent's extras.
     */
    fun argsFrom(intent: Intent): T = argsFrom(intent.extras)

    @OptIn(InternalDestinationsApi::class)
    override val style: DestinationStyle
        get() = DestinationStyle.Activity

    // region inherited that will never be used for activity destinations
    // ideally, we should have an additional level in the hierarchy, but at the point we are
    // this is just easier and safer
    override fun argsFrom(navBackStackEntry: NavBackStackEntry): T =
        error("unexpected error: calling NavBackStackEntry based argsFrom method on ActivityDestination!")

    override fun argsFrom(savedStateHandle: SavedStateHandle): T =
        error("unexpected error: calling SavedStateHandle based argsFrom method on ActivityDestination!")

    @Composable
    override fun DestinationScope<T>.Content(dependenciesContainerBuilder: DependenciesContainerBuilder<T>.() -> Unit) {
        error("unexpected error: calling Content method ActivityDestination!")
    }
    // endregion
}