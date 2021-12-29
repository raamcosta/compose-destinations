package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.ClassType

const val CORE_PACKAGE_NAME = "com.ramcosta.composedestinations"

const val DESTINATION_ANNOTATION = "Destination"
const val NAV_TYPE_SERIALIZER_ANNOTATION = "NavTypeSerializer"
const val DESTINATION_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.annotation.$DESTINATION_ANNOTATION"
const val NAV_TYPE_SERIALIZER_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.navargs.$NAV_TYPE_SERIALIZER_ANNOTATION"

const val DESTINATION_ANNOTATION_ROUTE_ARGUMENT = "route"
const val DESTINATION_ANNOTATION_START_ARGUMENT = "start"
const val DESTINATION_ANNOTATION_NAV_GRAPH_ARGUMENT = "navGraph"
const val DESTINATION_ANNOTATION_STYLE_ARGUMENT = "style"
const val DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT = "navArgsDelegate"
const val DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT = "deepLinks"

const val DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER = "@ramcosta.destinations.fullroute@" // Needs to be the same as the constant in core module
const val DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER = "@ramcosta.destinations.composable-name-route@" // Needs to be the same as the constant in core module's Destination

const val CORE_EXTENSIONS = "CoreExtensions"
const val GENERATED_DESTINATION = "TypedDestination"
const val GENERATED_NO_ARGS_DESTINATION = "DirectionDestination"
const val GENERATED_NAV_GRAPH = "NavGraph"
const val GENERATED_NAV_GRAPHS_OBJECT = "NavGraphs"
const val GENERATED_DESTINATION_SUFFIX = "Destination"

const val CORE_NAV_DESTINATIONS_NAVIGATION = "DestinationsNavController"
const val CORE_NAV_DESTINATIONS_NAVIGATION_QUALIFIED_NAME = "$CORE_PACKAGE_NAME.navigation.$CORE_NAV_DESTINATIONS_NAVIGATION"
const val CORE_DESTINATION_SPEC = "DestinationSpec"
const val CORE_NAV_GRAPH_SPEC = "NavGraphSpec"
const val CORE_DIRECTION = "Direction"
const val CORE_DESTINATION_ANIMATION_STYLE = "DestinationStyle.Animated"
const val CORE_BOTTOM_SHEET_DESTINATION_STYLE = "DestinationStyle.BottomSheet"

private const val EXPERIMENTAL_ANIMATION_API_SIMPLE_NAME = "ExperimentalAnimationApi"
private const val EXPERIMENTAL_ANIMATION_API_QUALIFIED_NAME = "androidx.compose.animation.ExperimentalAnimationApi"
val experimentalAnimationApiType = ClassType(
    simpleName = EXPERIMENTAL_ANIMATION_API_SIMPLE_NAME,
    qualifiedName = EXPERIMENTAL_ANIMATION_API_QUALIFIED_NAME
)

const val DESTINATIONS_NAVIGATOR_QUALIFIED_NAME = "$CORE_PACKAGE_NAME.navigation.DestinationsNavigator"
const val NAV_CONTROLLER_QUALIFIED_NAME = "androidx.navigation.NavController"
const val NAV_HOST_CONTROLLER_QUALIFIED_NAME = "androidx.navigation.NavHostController"
const val SAVED_STATE_HANDLE_SIMPLE_NAME = "SavedStateHandle"
const val SAVED_STATE_HANDLE_QUALIFIED_NAME = "androidx.lifecycle.SavedStateHandle"
const val NAV_BACK_STACK_ENTRY_SIMPLE_NAME = "NavBackStackEntry"
const val NAV_BACK_STACK_ENTRY_QUALIFIED_NAME = "androidx.navigation.$NAV_BACK_STACK_ENTRY_SIMPLE_NAME"
const val ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME = "AnimatedVisibilityScope"
const val ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME = "androidx.compose.animation.$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME"
const val COLUMN_SCOPE_SIMPLE_NAME = "ColumnScope"
const val COLUMN_SCOPE_QUALIFIED_NAME = "androidx.compose.foundation.layout.$COLUMN_SCOPE_SIMPLE_NAME"

const val CORE_ANIMATIONS_DEPENDENCY = "io.github.raamcosta.compose-destinations:animations-core"