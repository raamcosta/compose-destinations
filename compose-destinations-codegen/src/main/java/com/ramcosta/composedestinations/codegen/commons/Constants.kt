package com.ramcosta.composedestinations.codegen.commons

const val PACKAGE_NAME = "com.ramcosta.composedestinations"

const val DESTINATION_ANNOTATION = "Destination"
const val DESTINATION_ANNOTATION_QUALIFIED = "$PACKAGE_NAME.annotation.$DESTINATION_ANNOTATION"

const val DESTINATION_ANNOTATION_ROUTE_ARGUMENT = "route"
const val DESTINATION_ANNOTATION_START_ARGUMENT = "start"
const val DESTINATION_ANNOTATION_NAV_GRAPH_ARGUMENT = "navGraph"
const val DESTINATION_ANNOTATION_STYLE_ARGUMENT = "style"
const val DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT = "deepLinks"

const val DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER = "@ramcosta.destinations.fullroute@" // Needs to be the same as the constant in core module
const val DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER = "@composable-name-route" // Needs to be the same as the constant in core module's Destination

const val CORE_EXTENSIONS = "CoreExtensions"
const val CORE_ANIMATION_EXTENSIONS = "CoreAnimationExtensions"
const val CORE_BOTTOM_SHEET_EXTENSIONS = "CoreBottomSheetExtensions"
const val GENERATED_DESTINATION = "Destination"
const val GENERATED_NAV_GRAPH = "NavGraph"
const val GENERATED_NAV_GRAPHS_OBJECT = "NavGraphs"
const val GENERATED_DESTINATION_SUFFIX = "Destination"
const val GENERATED_ANIMATED_DESTINATION_STYLE = "AnimatedDestinationStyle"

const val CORE_NAV_DESTINATIONS_NAVIGATION = "NavControllerDestinationsNavigator"
const val CORE_NAV_DESTINATIONS_NAVIGATION_QUALIFIED_NAME = "$PACKAGE_NAME.navigation.$CORE_NAV_DESTINATIONS_NAVIGATION"
const val CORE_DESTINATION_SPEC = "DestinationSpec"
const val CORE_NAV_GRAPH_SPEC = "NavGraphSpec"
const val CORE_DESTINATION_ANIMATION_STYLE = "DestinationStyle.Animated"
const val CORE_BOTTOM_SHEET_DESTINATION_STYLE = "DestinationStyle.BottomSheet"

const val DESTINATIONS_NAV_HOST = "DestinationsNavHost"

const val EXPERIMENTAL_ANIMATION_API_SIMPLE_NAME = "ExperimentalAnimationApi"
const val EXPERIMENTAL_ANIMATION_API_QUALIFIED_NAME = "androidx.compose.animation.ExperimentalAnimationApi"
const val DESTINATIONS_NAVIGATOR_QUALIFIED_NAME = "$PACKAGE_NAME.navigation.DestinationsNavigator"
const val NAV_CONTROLLER_QUALIFIED_NAME = "androidx.navigation.NavController"
const val NAV_HOST_CONTROLLER_QUALIFIED_NAME = "androidx.navigation.NavHostController"
const val NAV_BACK_STACK_ENTRY_QUALIFIED_NAME = "androidx.navigation.NavBackStackEntry"
const val ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME = "AnimatedVisibilityScope"
const val ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME = "androidx.compose.animation.$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME"
const val COLUMN_SCOPE_SIMPLE_NAME = "ColumnScope"
const val COLUMN_SCOPE_QUALIFIED_NAME = "androidx.compose.foundation.layout.$COLUMN_SCOPE_SIMPLE_NAME"

const val COMPOSE_NAVIGATION = "androidx.navigation:navigation-compose"
const val ACCOMPANIST_NAVIGATION_MATERIAL = "com.google.accompanist:accompanist-navigation-material"
const val ACCOMPANIST_NAVIGATION_ANIMATION = "com.google.accompanist:accompanist-navigation-animation"
