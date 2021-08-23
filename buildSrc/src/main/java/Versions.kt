object Versions {
    const val minSdk = 21
    const val compileSdk = 30
    const val targetSdk = compileSdk

    const val kotlin = "1.5.21"
    const val compose = "1.0.1"

    const val material = "1.4.0"
    const val activityCompose = "1.3.0"
    const val lifecycleKtx = "2.4.0-alpha01"
    const val lifecycleRuntimeKtx = lifecycleKtx
}

object Deps {

    object Gradle {
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    }

    object Android {
        const val material = "com.google.android.material:material:${Versions.material}"
    }

    object AndroidX {
        const val lifecycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleRuntimeKtx}"
        const val activityCompose = "androidx.activity:activity-compose:${Versions.activityCompose}"
    }

    object Compose {
        const val ui = "androidx.compose.ui:ui:${Versions.compose}"
        const val material = "androidx.compose.material:material:${Versions.compose}"
    }
}