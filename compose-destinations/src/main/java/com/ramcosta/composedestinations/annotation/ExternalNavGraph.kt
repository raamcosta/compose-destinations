package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavGraphSpec
import kotlin.reflect.KClass

/**
 * Can be used in a companion object of a [NavGraph] annotated annotation
 * to include a [NavGraphSpec] from another module as nested in the navigation graph.
 *
 * Example:
 *
 * ```
 * @NavGraph<RootGraph>
 * annotation class ProfileGraph {
 *     @ExternalNavGraph<AnotherModuleNavGraph>
 *     companion object Includes // name of the companion object does not matter
 * }
 * ```
 *
 * This would create a "Profile" nav graph nested on "Root" graph
 * and with "AnotherModuleNavGraph" as a nested graph of "Profile".
 *
 * @param T the [NavGraphSpec] from another module to include in the navigation graph
 * @param start defines this navigation graph as the start of the navigation graph
 * it is being included on.
 * @param deepLinks adds [DeepLink]s to this nav graph. Both these and the deep links
 * defined on the declaring module (if any) can be used to navigate to this nav graph.
 * @param defaultTransitions overrides animations set on the declaring module (if any).
 * See [NavGraph.defaultTransitions] for more info on these animations.
 */
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class ExternalNavGraph<T: NavGraphSpec>(
    val start: Boolean = false,
    val deepLinks: Array<DeepLink> = [],
    val defaultTransitions: KClass<out DestinationStyle.Animated> = NoOverride::class,
) {
    companion object {
        internal object NoOverride: DestinationStyle.Animated()
    }
}