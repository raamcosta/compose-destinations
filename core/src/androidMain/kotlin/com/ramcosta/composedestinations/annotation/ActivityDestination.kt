package com.ramcosta.composedestinations.annotation

import android.app.Activity
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import kotlin.reflect.KClass

/**
 * Like [Destination] but adds an [Activity] as a destination.
 *
 * @param route main route of this destination (by default, the name of the activity class)
 * @param navArgs class with a primary constructor where all navigation arguments are
 * to be defined.
 * The generated `Destination` class has `argsFrom` methods that accept an `Intent` and return an
 * instance of this class.
 * @param deepLinks array of [DeepLink] which can be used to navigate to this destination
 * @param activityClass the class corresponding to the [Activity] to be registered as a destination.
 * If you annotate an [Activity] class with this, then you cannot specify this. However, if you annotate
 * a non-activity class, then you'll need to pass this (useful for external activities that you cannot
 * annotate).
 * @param targetPackage see [androidx.navigation.ActivityNavigator.Destination.targetPackage]
 * @param dataUri see [androidx.navigation.ActivityNavigator.Destination.data]
 * @param dataPattern see [androidx.navigation.ActivityNavigator.Destination.dataPattern]
 * @param visibility [CodeGenVisibility] of the corresponding generated Destination object.
 * Useful to control what the current module exposes to other modules. By default, it is public.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ActivityDestination<T: Annotation>(
    val route: String = Destination.COMPOSABLE_NAME,
    val start: Boolean = false,
    val navArgs: KClass<*> = Nothing::class,
    val deepLinks: Array<DeepLink> = [],
    val activityClass: KClass<out Activity> = Nothing::class,
    val targetPackage: String = DEFAULT_NULL,
    val action: String = DEFAULT_NULL,
    val dataUri: String = DEFAULT_NULL,
    val dataPattern: String = DEFAULT_NULL,
    val visibility: CodeGenVisibility = CodeGenVisibility.PUBLIC
) {
    companion object {
        const val DEFAULT_NULL = "@ramcosta.destinations.activity-null-default@"
    }
}

/**
 * Like [ActivityDestination] but specifically for java files, since
 * java doesn't have annotation with type arguments, so nav graph can't be
 * set in that manner.
 * Use [navGraph] instead to pass that same annotation class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class JavaActivityDestination(
    /* keep in sync with ActivityDestination fields */
    val navGraph: KClass<out Annotation>,
    val route: String = Destination.COMPOSABLE_NAME,
    val start: Boolean = false,
    val navArgs: KClass<*> = Nothing::class,
    val deepLinks: Array<DeepLink> = [],
    val activityClass: KClass<out Activity> = Nothing::class,
    val targetPackage: String = ActivityDestination.DEFAULT_NULL,
    val action: String = ActivityDestination.DEFAULT_NULL,
    val dataUri: String = ActivityDestination.DEFAULT_NULL,
    val dataPattern: String = ActivityDestination.DEFAULT_NULL,
    val visibility: CodeGenVisibility = CodeGenVisibility.PUBLIC
)
