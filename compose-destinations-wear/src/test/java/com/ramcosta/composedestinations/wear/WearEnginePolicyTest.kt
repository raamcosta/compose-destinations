package com.ramcosta.composedestinations.wear

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.manualcomposablecalls.allDeepLinks
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.spec.DeepLinksContext
import com.ramcosta.composedestinations.spec.DefaultEnginePolicy
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.TypedDestinationSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class WearEnginePolicyTest {

    @Test
    fun `policy style override is applied in wear resolution flow`() {
        val destination = TestDestination(route = "inbox")
        val manualOverride = ManualAnimatedStyle
        val policyOverride = PolicyAnimatedStyle

        val manualCalls = manualComposableCalls(
            animations = mapOf(destination.route to manualOverride),
            enginePolicy = object : DefaultEnginePolicy {
                override fun resolveDestinationStyle(ctx: com.ramcosta.composedestinations.spec.DestinationStyleContext): DestinationStyle? {
                    return policyOverride
                }
            },
            destinationParentRoutes = mapOf(destination.route to "root"),
        )

        val resolvedStyle = manualCalls.resolveDestinationStyle(destination)
        assertSame(policyOverride, resolvedStyle)
        requireSupportedWearStyle(resolvedStyle)
    }

    @Test
    fun `wear rejects unsupported policy style`() {
        val destination = TestDestination(route = "dialogTarget")
        val manualCalls = manualComposableCalls(
            enginePolicy = object : DefaultEnginePolicy {
                override fun resolveDestinationStyle(ctx: com.ramcosta.composedestinations.spec.DestinationStyleContext): DestinationStyle? {
                    return DestinationStyle.Dialog.Default
                }
            }
        )

        val resolvedStyle = manualCalls.resolveDestinationStyle(destination)
        assertThrows(IllegalStateException::class.java) {
            requireSupportedWearStyle(resolvedStyle)
        }
    }

    @Test
    fun `wear deep links use transformed policy output`() {
        val generatedUri = "wear://generated"
        val manualUri = "wear://manual"
        val policyUri = "wear://policy"
        val destination = TestDestination(
            route = "feature",
            deepLinks = listOf(navDeepLink { uriPattern = generatedUri })
        )

        val manualCalls = manualComposableCalls(
            deepLinks = mapOf(destination.route to listOf(navDeepLink { uriPattern = manualUri })),
            enginePolicy = object : DefaultEnginePolicy {
                override fun transformDeepLinks(ctx: DeepLinksContext): List<NavDeepLink>? {
                    return listOf(navDeepLink { uriPattern = policyUri })
                }
            }
        )

        assertEquals(
            listOf(policyUri),
            destination.allDeepLinks(manualCalls).map { it.uriPattern }
        )
    }
}

private object ManualAnimatedStyle : DestinationStyle.Animated()
private object PolicyAnimatedStyle : DestinationStyle.Animated()

private class TestDestination(
    override val route: String,
    override val style: DestinationStyle = DestinationStyle.Default,
    override val deepLinks: List<NavDeepLink> = emptyList(),
) : TypedDestinationSpec<Unit> {

    override val baseRoute: String = route

    override fun invoke(navArgs: Unit): Direction = Direction(baseRoute)

    override fun argsFrom(bundle: Bundle?): Unit = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle): Unit = Unit

    @Composable
    override fun DestinationScope<Unit>.Content() = Unit
}

@Suppress("UNCHECKED_CAST")
private fun manualComposableCalls(
    map: Map<String, Any> = emptyMap(),
    animations: Map<String, DestinationStyle.Animated> = emptyMap(),
    deepLinks: Map<String, List<NavDeepLink>> = emptyMap(),
    destinationParentRoutes: Map<String, String> = emptyMap(),
    enginePolicy: DefaultEnginePolicy = DefaultEnginePolicy.None,
): ManualComposableCalls {
    val constructor = ManualComposableCalls::class.java.declaredConstructors.single()
    constructor.isAccessible = true
    return constructor.newInstance(
        map,
        animations,
        deepLinks,
        destinationParentRoutes,
        enginePolicy,
    ) as ManualComposableCalls
}
