package com.ramcosta.samples.destinationstodosample

import android.app.Application
import com.ramcosta.samples.destinationstodosample.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.module

class DestinationsSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Start Koin
        startKoin {
            androidLogger()
            androidContext(this@DestinationsSampleApplication)
            modules(AppModule().module)
        }
    }
}