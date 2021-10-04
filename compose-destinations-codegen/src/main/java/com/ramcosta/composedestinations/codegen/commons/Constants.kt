package com.ramcosta.composedestinations.codegen.commons

const val PACKAGE_NAME = "com.ramcosta.composedestinations"

const val DESTINATION_ANNOTATION = "Destination"
const val DESTINATION_ANNOTATION_QUALIFIED = "$PACKAGE_NAME.annotation.$DESTINATION_ANNOTATION"
const val DESTINATION_TRANSITIONS_SPEC_ANNOTATION = "DestinationTransitions"
const val DESTINATION_TRANSITIONS_SPEC_ANNOTATION_QUALIFIED = "$PACKAGE_NAME.annotation.$DESTINATION_TRANSITIONS_SPEC_ANNOTATION"
const val DESTINATION_ANNOTATION_ROUTE_ARGUMENT = "route"
const val DESTINATION_ANNOTATION_START_ARGUMENT = "start"
const val DESTINATION_ANNOTATION_NAV_GRAPH_ARGUMENT = "navGraph"
const val DESTINATION_ANNOTATION_DEEP_LINKS = "deepLinks"
const val DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER = "@ramcosta.destinations.fullroute@" // Needs to be the same as the constant in core module

const val GENERATED_DESTINATION = "Destination"
const val GENERATED_NAV_GRAPH = "NavGraph"
const val GENERATED_DESTINATION_SUFFIX = "Destination"
const val GENERATED_DESTINATION_TRANSITIONS = "DestinationTransitionsSpec"

const val CORE_NAV_DESTINATIONS_NAVIGATION = "NavControllerDestinationsNavigator"
const val CORE_NAV_DESTINATIONS_NAVIGATION_QUALIFIED_NAME = "$PACKAGE_NAME.navigation.$CORE_NAV_DESTINATIONS_NAVIGATION"
const val CORE_DESTINATION_SPEC = "DestinationSpec"
const val CORE_NAV_GRAPH_SPEC = "NavGraphSpec"

const val DESTINATIONS_AGGREGATE_CLASS_NAME = "Destinations"

const val DESTINATIONS_NAVIGATOR_QUALIFIED_NAME = "$PACKAGE_NAME.navigation.DestinationsNavigator"
const val NAV_CONTROLLER_QUALIFIED_NAME = "androidx.navigation.NavController"
const val NAV_BACK_STACK_ENTRY_QUALIFIED_NAME = "androidx.navigation.NavBackStackEntry"
const val SCAFFOLD_STATE_QUALIFIED_NAME = "androidx.compose.material.ScaffoldState"
const val ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME = "AnimatedVisibilityScope"
