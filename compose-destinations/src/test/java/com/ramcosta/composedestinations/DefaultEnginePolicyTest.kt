package com.ramcosta.composedestinations

import android.app.Activity
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.navDeepLink
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.allDeepLinks
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.spec.ActivityDestinationSpec
import com.ramcosta.composedestinations.spec.DeepLinksContext
import com.ramcosta.composedestinations.spec.DefaultEnginePolicy
import com.ramcosta.composedestinations.spec.DefaultEnginePolicyCapable
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.GraphTransitionsContext
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.ramcosta.composedestinations.spec.TypedDestinationSpec
import com.ramcosta.composedestinations.spec.TypedNavGraphSpec
import com.ramcosta.composedestinations.spec.TypedRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class DefaultEnginePolicyTest {

    @Test
    fun `destination style precedence is policy over manual over default`() {
        val destination = TestDestination(route = "profile", style = DestinationStyle.Default)
        val parentGraph = TestNavGraph(
            route = "root",
            startRoute = destination,
            destinations = listOf(destination),
            defaultTransitions = null,
        )

        val manualOverride = ManualTransitionStyle
        val policyOverride = DestinationStyle.Dialog.Default

        val manualCalls = ManualComposableCallsBuilder(NavHostEngine.Type.DEFAULT)
            .apply { destination animateWith manualOverride }
            .build(
                destinationParentRoutes = mapOf(destination.route to parentGraph.route),
                enginePolicy = object : DefaultEnginePolicy {
                    override fun resolveDestinationStyle(ctx: com.ramcosta.composedestinations.spec.DestinationStyleContext): DestinationStyle? {
                        return policyOverride
                    }
                }
            )

        assertSame(policyOverride, manualCalls.resolveDestinationStyle(destination))

        val noPolicy = ManualComposableCallsBuilder(NavHostEngine.Type.DEFAULT)
            .apply { destination animateWith manualOverride }
            .build()
        assertSame(manualOverride, noPolicy.resolveDestinationStyle(destination))

        val noPolicyNoManual = ManualComposableCallsBuilder(NavHostEngine.Type.DEFAULT).build()
        assertSame(destination.style, noPolicyNoManual.resolveDestinationStyle(destination))
    }

    @Test
    fun `graph transitions precedence is policy over manual over default`() {
        val destination = TestDestination(route = "home")
        val navGraph = TestNavGraph(
            route = "feature",
            startRoute = destination,
            destinations = listOf(destination),
            defaultTransitions = DefaultGraphTransitions,
        )
        val manualOverride = ManualGraphTransitions
        val policyOverride = PolicyGraphTransitions

        val manualCalls = ManualComposableCallsBuilder(NavHostEngine.Type.DEFAULT)
            .apply { navGraph animateWith manualOverride }
            .build(
                enginePolicy = object : DefaultEnginePolicy {
                    override fun resolveGraphTransitions(ctx: GraphTransitionsContext): DestinationStyle.Animated? {
                        return policyOverride
                    }
                }
            )
        assertSame(policyOverride, manualCalls.resolveGraphTransitions(navGraph))

        val noPolicy = ManualComposableCallsBuilder(NavHostEngine.Type.DEFAULT)
            .apply { navGraph animateWith manualOverride }
            .build()
        assertSame(manualOverride, noPolicy.resolveGraphTransitions(navGraph))

        val noPolicyNoManual = ManualComposableCallsBuilder(NavHostEngine.Type.DEFAULT).build()
        assertSame(DefaultGraphTransitions, noPolicyNoManual.resolveGraphTransitions(navGraph))
    }

    @Test
    fun `deep links baseline is manual then generated and policy may replace or keep it`() {
        val generatedUri = "app://generated"
        val manualUri = "app://manual"
        val replacementUri = "app://policy"
        val destination = TestDestination(
            route = "details",
            deepLinks = listOf(navDeepLink { uriPattern = generatedUri })
        )

        val baselineCalls = ManualComposableCallsBuilder(NavHostEngine.Type.DEFAULT)
            .apply { destination addDeepLink { uriPattern = manualUri } }
            .build()

        assertEquals(
            listOf(manualUri, generatedUri),
            destination.allDeepLinks(baselineCalls).map { it.uriPattern }
        )

        val replacingCalls = ManualComposableCallsBuilder(NavHostEngine.Type.DEFAULT)
            .apply { destination addDeepLink { uriPattern = manualUri } }
            .build(
                enginePolicy = object : DefaultEnginePolicy {
                    override fun transformDeepLinks(ctx: DeepLinksContext): List<NavDeepLink>? {
                        return listOf(navDeepLink { uriPattern = replacementUri })
                    }
                }
            )

        assertEquals(
            listOf(replacementUri),
            destination.allDeepLinks(replacingCalls).map { it.uriPattern }
        )

        val keepingCalls = ManualComposableCallsBuilder(NavHostEngine.Type.DEFAULT)
            .apply { destination addDeepLink { uriPattern = manualUri } }
            .build(
                enginePolicy = object : DefaultEnginePolicy {
                    override fun transformDeepLinks(ctx: DeepLinksContext): List<NavDeepLink>? = null
                }
            )

        assertEquals(
            listOf(manualUri, generatedUri),
            destination.allDeepLinks(keepingCalls).map { it.uriPattern }
        )
    }

    @Test
    fun `policy compatibility guard fails for non-capable engines`() {
        assertThrows(IllegalStateException::class.java) {
            validateEnginePolicySupport(
                engine = NonPolicyCapableEngine(),
                enginePolicy = object : DefaultEnginePolicy {},
            )
        }

        validateEnginePolicySupport(
            engine = NonPolicyCapableEngine(),
            enginePolicy = DefaultEnginePolicy.None,
        )

        validateEnginePolicySupport(
            engine = PolicyCapableEngine(),
            enginePolicy = object : DefaultEnginePolicy {},
        )
    }

    @Test
    fun `default engine activity style compatibility checks`() {
        val regularDestination = TestDestination(route = "regular")
        assertThrows(IllegalStateException::class.java) {
            validateDefaultEngineStyleCompatibility(regularDestination, DestinationStyle.Activity)
        }

        val activityDestination = TestActivityDestination(route = "external")
        assertThrows(IllegalStateException::class.java) {
            validateDefaultEngineStyleCompatibility(activityDestination, DestinationStyle.Default)
        }

        validateDefaultEngineStyleCompatibility(activityDestination, DestinationStyle.Activity)
    }
}

private object ManualTransitionStyle : DestinationStyle.Animated()
private object DefaultGraphTransitions : DestinationStyle.Animated()
private object ManualGraphTransitions : DestinationStyle.Animated()
private object PolicyGraphTransitions : DestinationStyle.Animated()

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

private class TestActivityDestination(
    override val route: String,
    override val deepLinks: List<NavDeepLink> = emptyList(),
) : ActivityDestinationSpec<Unit> {

    override val baseRoute: String = route
    override val activityClass: Class<out Activity> = Activity::class.java

    override fun invoke(navArgs: Unit): Direction = Direction(baseRoute)

    override fun argsFrom(bundle: Bundle?): Unit = Unit
}

private class TestNavGraph(
    override val route: String,
    override val startRoute: TypedRoute<Unit>,
    override val destinations: List<com.ramcosta.composedestinations.spec.DestinationSpec>,
    override val defaultTransitions: DestinationStyle.Animated?,
    override val deepLinks: List<NavDeepLink> = emptyList(),
    override val nestedNavGraphs: List<NavGraphSpec> = emptyList(),
) : TypedNavGraphSpec<Unit, Unit> {

    override val baseRoute: String = route

    override fun invoke(navArgs: Unit): Direction = Direction(route)

    override fun argsFrom(bundle: Bundle?): Unit? = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle): Unit? = Unit
}

private open class BaseFakeEngine : NavHostEngine {

    override val type: NavHostEngine.Type = NavHostEngine.Type.DEFAULT

    @Composable
    override fun rememberNavController(vararg navigators: Navigator<out NavDestination>): NavHostController {
        error("not used in unit tests")
    }

    @Composable
    override fun NavHost(
        modifier: Modifier,
        route: String,
        start: Direction,
        defaultTransitions: com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle,
        navController: NavHostController,
        builder: NavGraphBuilder.() -> Unit,
    ) {
        error("not used in unit tests")
    }

    override fun NavGraphBuilder.navigation(
        navGraph: NavGraphSpec,
        manualComposableCalls: com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls,
        builder: NavGraphBuilder.() -> Unit,
    ) {
        error("not used in unit tests")
    }

    override fun <T> NavGraphBuilder.composable(
        destination: TypedDestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls,
    ) {
        error("not used in unit tests")
    }
}

private class NonPolicyCapableEngine : BaseFakeEngine()

private class PolicyCapableEngine : BaseFakeEngine(), DefaultEnginePolicyCapable
