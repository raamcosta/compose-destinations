package com.ramcosta.samples.playground.di

import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

//@Composable
//inline fun <reified VM : ViewModel> activityViewModel(): VM {
//    val activity = LocalActivity
//    return ViewModelProvider(
//        owner = activity,
//        factory = ViewModelFactory(
//            LocalDIContainer.current,
//            activity
//        )
//    )[VM::class.java]
//}

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