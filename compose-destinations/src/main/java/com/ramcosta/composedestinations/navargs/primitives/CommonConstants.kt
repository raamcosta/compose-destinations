package com.ramcosta.composedestinations.navargs.primitives

import android.net.Uri

const val ENCODED_NULL = "%@null@"
val DECODED_NULL: String = Uri.decode(ENCODED_NULL)