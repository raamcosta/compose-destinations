package com.ramcosta.composedestinations.dynamic.destination

import com.ramcosta.composedestinations.annotation.InternalDestinationsApi
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

@InternalDestinationsApi
interface DynamicDestinationSpec<T> : TypedDestinationSpec<T> {
    val originalDestination: TypedDestinationSpec<T>
}

@OptIn(InternalDestinationsApi::class)
@PublishedApi
internal val <T> TypedDestinationSpec<T>.originalDestination
    get(): TypedDestinationSpec<T> =
        if (this is DynamicDestinationSpec<T>) {
            this.originalDestination
        } else {
            this
        }
