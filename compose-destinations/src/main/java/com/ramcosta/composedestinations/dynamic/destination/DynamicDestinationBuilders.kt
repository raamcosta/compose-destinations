package com.ramcosta.composedestinations.dynamic.destination

import androidx.compose.runtime.Composable
import androidx.navigation.NavDeepLink
import com.ramcosta.composedestinations.annotation.InternalDestinationsApi
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.TypedDestinationSpec
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import com.ramcosta.composedestinations.wrapper.Wrap

@InternalDestinationsApi
class DynamicDestinationBuilder<T>(
    val originalDestinationSpec: TypedDestinationSpec<T>
) {
    var style: DestinationStyle? = null
    var additionalWrappers: Array<DestinationWrapper>? = null
    var additionalDeepLinks: List<NavDeepLink>? = null

    fun build() : DynamicDestinationSpec<T> {
        return object: DynamicDestinationSpec<T>, TypedDestinationSpec<T> by originalDestinationSpec {
            override val originalDestination: TypedDestinationSpec<T> = originalDestinationSpec.originalDestination

            override val style: DestinationStyle
                get() = this@DynamicDestinationBuilder.style ?: originalDestination.style

            override val deepLinks: List<NavDeepLink>
                get() = this@DynamicDestinationBuilder.additionalDeepLinks.orEmpty() + originalDestination.deepLinks

            @Composable
            override fun DestinationScope<T>.Content() {
                val wrappers = additionalWrappers
                if (wrappers == null) {
                    with(originalDestination) { Content() }
                } else {
                    Wrap(*wrappers) {
                        with(originalDestination) { Content() }
                    }
                }
            }
        }
    }
}

@InternalDestinationsApi
fun <T> TypedDestinationSpec<T>.with(builder: DynamicDestinationBuilder<T>.() -> Unit): DynamicDestinationSpec<T> {
    return DynamicDestinationBuilder(this).apply { builder() }.build()
}