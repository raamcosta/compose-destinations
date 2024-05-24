package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.wrapper.DestinationWrapper
import kotlin.reflect.KClass

/**
 * Can be used in a companion object of a [NavGraph] annotated annotation
 * to include a [DestinationSpec] from another module in the navigation graph.
 *
 * Example:
 *
 * ```
 * @NavGraph<RootGraph>
 * annotation class ProfileGraph {
 *     @ExternalDestination<AnotherModuleDestination>
 *     companion object Includes // name of the companion object does not matter
 * }
 * ```
 *
 * This would create a "Profile" nav graph nested on "Root" graph
 * and with "AnotherModuleDestination" as a destination part of "Profile".
 *
 * @param T the [DestinationSpec] from another module to include in the navigation graph
 * @param start defines this destination as the start of the navigation graph
 * it is being included on.
 * @param deepLinks adds [DeepLink]s to this destination. Both these and the deep links
 * defined on the declaring module (if any) can be used to navigate to this destination.
 * @param style overrides [DestinationStyle] for this destination. The defined style
 * on the declaring module will be ignored.
 * @param wrappers adds [DestinationWrapper]. Both these and the ones defined on the declaring
 * module will be applied.
 */
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class ExternalDestination<T: DestinationSpec>(
    val start: Boolean = false,
    val deepLinks: Array<DeepLink> = [],
    val style: KClass<out DestinationStyle> = Nothing::class,
    val wrappers: Array<KClass<out DestinationWrapper>> = [],
)
