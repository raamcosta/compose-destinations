object Versions {
    const val minSdk = 21
    const val compileSdk = 31
    const val targetSdk = compileSdk

    const val kotlin = "1.5.31"
    const val dependencyCheckPlugin = "0.39.0"
    const val gradlePluginVersion = "7.0.3"

    const val compose = "1.0.4"
    const val composeNavigation = "2.4.0-alpha10"
    const val composeViewModel = "2.4.0-rc01"
    const val activityCompose = "1.3.1"

    const val accompanist = "0.20.0"

    const val material = "1.4.0"
    const val lifecycleKtx = "2.4.0-rc01"
    const val lifecycleRuntimeKtx = lifecycleKtx

    const val ksp = "1.5.31-1.0.0"

    const val junit = "4.13.2"
}

object Deps {

    object Gradle {
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
        const val dependencyCheckPlugin = "com.github.ben-manes.versions"
        const val pluginVersion = "com.android.tools.build:gradle:${Versions.gradlePluginVersion}"
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
        const val navigation = "androidx.navigation:navigation-compose:${Versions.composeNavigation}"
        const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.composeViewModel}"

        const val accompanistMaterial = "com.google.accompanist:accompanist-navigation-material:${Versions.accompanist}"
        const val accompanistAnimation = "com.google.accompanist:accompanist-navigation-animation:${Versions.accompanist}"

    }

    object Ksp {
        const val api = "com.google.devtools.ksp:symbol-processing-api:${Versions.ksp}"
    }

    object Test {
        const val junit = "junit:junit:${Versions.junit}"
    }
}