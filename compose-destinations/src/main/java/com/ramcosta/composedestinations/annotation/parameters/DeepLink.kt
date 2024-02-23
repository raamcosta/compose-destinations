package com.ramcosta.composedestinations.annotation.parameters

/**
 * Can be used in [com.ramcosta.composedestinations.annotation.Destination] to describe a deep link
 * connection to that destination.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class DeepLink(
    val action: String = "",
    val mimeType: String = "",
    val uriPattern: String = ""
)

/**
 * Can be used in the suffix part of the [DeepLink.uriPattern]
 * to signal code generation to replace this with the full
 * route of the [com.ramcosta.composedestinations.annotation.Destination] that contains arguments.
 *
 * Example:
 *
 * ```
 * @Destination(
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