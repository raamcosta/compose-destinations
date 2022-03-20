package com.ramcosta.composedestinations.result

import com.ramcosta.composedestinations.spec.DestinationSpec

internal fun <D : DestinationSpec<*>, R> resultKey(
    resultOriginType: Class<D>,
    resultType: Class<R>
) = "compose-destinations@${resultOriginType.name}@${resultType.name}@result"

internal fun <D : DestinationSpec<*>, R> canceledKey(
    resultOriginType: Class<D>,
    resultType: Class<R>
) = "compose-destinations@${resultOriginType.name}@${resultType.name}@canceled"