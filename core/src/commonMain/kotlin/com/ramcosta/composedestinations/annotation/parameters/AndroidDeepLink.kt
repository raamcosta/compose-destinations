@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.ramcosta.composedestinations.annotation.parameters

/**
 * Can be used in [com.ramcosta.composedestinations.annotation.Destination] to add a deep link
 * connection to that destination.
 *
 * Currently deep links are only supported on android, so this will only be added on that target.
 * For other targets it doesn't do anything.
 *
 * You can also add deep links at runtime with
 * [com.ramcosta.composedestinations.manualcomposablecalls.addDeepLink] only available on android
 * source set. In that case, you should create a expect fun on common code and on the android actual
 * function use `addDeepLink`.
 */
@Retention(AnnotationRetention.SOURCE)
expect annotation class AndroidDeepLink(
    val action: String,
    val mimeType: String,
    val uriPattern: String
)

/**
 * Can be used in the suffix part of the [AndroidDeepLink.uriPattern]
 * to signal code generation to replace this with the full
 * route of the [com.ramcosta.composedestinations.annotation.Destination] that contains arguments.
 *
 * Example:
 *
 * ```
 * @Destination<RootGraph>(
 *      route = "profile",
 *      deepLinks = [
 *          DeepLink(
 *              uriPattern = "https://myapp.com/$FULL_ROUTE_PLACEHOLDER"
 *          )
 *      ]
 * )
 * @Composable
 * fun ProfileScreen(
 *      id: String
 * )
 * ```
 *
 * Since the `ProfileScreen` has an argument (`id`), the final uriPattern
 * used will be `"https://myapp.com/profile/{id}"`
 */
const val FULL_ROUTE_PLACEHOLDER = "@ramcosta.destinations.fullroute@"