package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams

const val CORE_PACKAGE_NAME = "com.ramcosta.composedestinations"

const val DESTINATION_ANNOTATION = "Destination"
const val NAV_GRAPH_ANNOTATION = "NavGraph"
const val NAV_TYPE_SERIALIZER_ANNOTATION = "NavTypeSerializer"
const val DESTINATION_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.annotation.$DESTINATION_ANNOTATION"
const val NAV_GRAPH_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.annotation.$NAV_GRAPH_ANNOTATION"
const val NAV_TYPE_SERIALIZER_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.navargs.$NAV_TYPE_SERIALIZER_ANNOTATION"

val rootNavGraphType = Importable(
    "RootNavGraph",
    "$CORE_PACKAGE_NAME.annotation.RootNavGraph"
)
val rootNavGraphGenParams = RawNavGraphGenParams(rootNavGraphType, true)

const val DESTINATION_ANNOTATION_ROUTE_ARGUMENT = "route"
const val DESTINATION_ANNOTATION_START_ARGUMENT = "start"
const val DESTINATION_ANNOTATION_NAV_GRAPH_ARGUMENT = "navGraph"
const val DESTINATION_ANNOTATION_STYLE_ARGUMENT = "style"
const val DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT = "navArgsDelegate"
const val DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT = "deepLinks"

const val DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER = "@ramcosta.destinations.fullroute@" // Needs to be the same as the constant in core module
const val DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER = "@ramcosta.destinations.composable-name-route@" // Needs to be the same as the constant in core module's Destination
const val NAV_GRAPH_ANNOTATION_DEFAULT_NAME = "@ramcosta.destinations.annotation-navgraph-route@" // Needs to be the same as the constant in core module's NavGraph

const val SINGLE_MODULE_EXTENSIONS = "SingleModuleExtensions"
const val NO_PREFIX_GENERATED_DESTINATION = "TypedDestination"
const val NO_PREFIX_GENERATED_NO_ARGS_DESTINATION = "DirectionDestination"
const val GENERATED_NAV_GRAPH = "NavGraph"
const val GENERATED_NAV_GRAPHS_OBJECT = "NavGraphs"
const val GENERATED_DESTINATION_SUFFIX = "Destination"

const val CORE_DESTINATION_SPEC = "DestinationSpec"
const val CORE_DIRECTION_DESTINATION_SPEC = "DirectionDestinationSpec"
const val CORE_NAV_GRAPH_SPEC = "NavGraphSpec"
val CORE_STRING_NAV_TYPE = Importable("DestinationsStringNavType", "$CORE_PACKAGE_NAME.navargs.primitives.DestinationsStringNavType")
val CORE_INT_NAV_TYPE = Importable("DestinationsIntNavType", "$CORE_PACKAGE_NAME.navargs.primitives.DestinationsIntNavType")
val CORE_BOOLEAN_NAV_TYPE = Importable("DestinationsBooleanNavType", "$CORE_PACKAGE_NAME.navargs.primitives.DestinationsBooleanNavType")
val CORE_FLOAT_NAV_TYPE = Importable("DestinationsFloatNavType", "$CORE_PACKAGE_NAME.navargs.primitives.DestinationsFloatNavType")
val CORE_LONG_NAV_TYPE = Importable("DestinationsLongNavType", "$CORE_PACKAGE_NAME.navargs.primitives.DestinationsLongNavType")
val CORE_LONG_ARRAY_NAV_TYPE = Importable("DestinationsLongArrayNavType", "$CORE_PACKAGE_NAME.navargs.primitives.array.DestinationsLongArrayNavType")
val CORE_BOOLEAN_ARRAY_NAV_TYPE = Importable("DestinationsBooleanArrayNavType", "$CORE_PACKAGE_NAME.navargs.primitives.array.DestinationsBooleanArrayNavType")
val CORE_INT_ARRAY_NAV_TYPE = Importable("DestinationsIntArrayNavType", "$CORE_PACKAGE_NAME.navargs.primitives.array.DestinationsIntArrayNavType")
val CORE_FLOAT_ARRAY_NAV_TYPE = Importable("DestinationsFloatArrayNavType", "$CORE_PACKAGE_NAME.navargs.primitives.array.DestinationsFloatArrayNavType")
val CORE_STRING_ARRAY_NAV_TYPE = Importable("DestinationsStringArrayNavType", "$CORE_PACKAGE_NAME.navargs.primitives.array.DestinationsStringArrayNavType")
val CORE_BOOLEAN_ARRAY_LIST_NAV_TYPE = Importable("DestinationsBooleanArrayListNavType", "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.DestinationsBooleanArrayListNavType")
val CORE_FLOAT_ARRAY_LIST_NAV_TYPE = Importable("DestinationsFloatArrayListNavType", "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.DestinationsFloatArrayListNavType")
val CORE_INT_ARRAY_LIST_NAV_TYPE = Importable("DestinationsIntArrayListNavType", "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.DestinationsIntArrayListNavType")
val CORE_LONG_ARRAY_LIST_NAV_TYPE = Importable("DestinationsLongArrayListNavType", "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.DestinationsLongArrayListNavType")
val CORE_STRING_ARRAY_LIST_NAV_TYPE = Importable("DestinationsStringArrayListNavType", "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.DestinationsStringArrayListNavType")
const val CORE_DIRECTION = "Direction"
const val CORE_DESTINATION_ANIMATION_STYLE = "DestinationStyle.Animated"
const val CORE_BOTTOM_SHEET_DESTINATION_STYLE = "DestinationStyle.BottomSheet"

private const val EXPERIMENTAL_ANIMATION_API_SIMPLE_NAME = "ExperimentalAnimationApi"
private const val EXPERIMENTAL_ANIMATION_API_QUALIFIED_NAME = "androidx.compose.animation.ExperimentalAnimationApi"
val experimentalAnimationApiType = Importable(
    simpleName = EXPERIMENTAL_ANIMATION_API_SIMPLE_NAME,
    qualifiedName = EXPERIMENTAL_ANIMATION_API_QUALIFIED_NAME
)

const val DESTINATIONS_NAVIGATOR_QUALIFIED_NAME = "$CORE_PACKAGE_NAME.navigation.DestinationsNavigator"
const val RESULT_BACK_NAVIGATOR_QUALIFIED_NAME = "$CORE_PACKAGE_NAME.result.ResultBackNavigator"
const val RESULT_RECIPIENT_QUALIFIED_NAME = "$CORE_PACKAGE_NAME.result.ResultRecipient"
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
