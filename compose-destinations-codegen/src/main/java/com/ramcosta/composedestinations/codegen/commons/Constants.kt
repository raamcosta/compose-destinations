package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.Visibility

const val CORE_PACKAGE_NAME = "com.ramcosta.composedestinations"

const val DESTINATION_ANNOTATION = "Destination"
const val ACTIVITY_DESTINATION_ANNOTATION = "ActivityDestination"
const val JAVA_ACTIVITY_DESTINATION_ANNOTATION = "JavaActivityDestination"
const val NAV_GRAPH_ANNOTATION = "NavGraph"
const val NAV_HOST_GRAPH_ANNOTATION = "NavHostGraph"
const val NAV_TYPE_SERIALIZER_ANNOTATION = "NavTypeSerializer"
const val DESTINATION_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.annotation.$DESTINATION_ANNOTATION"
const val NAV_GRAPH_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.annotation.$NAV_GRAPH_ANNOTATION"
const val NAV_HOST_GRAPH_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.annotation.$NAV_HOST_GRAPH_ANNOTATION"
const val ACTIVITY_DESTINATION_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.annotation.$ACTIVITY_DESTINATION_ANNOTATION"
const val JAVA_ACTIVITY_DESTINATION_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.annotation.$JAVA_ACTIVITY_DESTINATION_ANNOTATION"
const val NAV_HOST_PARAM_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.annotation.parameters.NavHostParam"
const val NAV_TYPE_SERIALIZER_ANNOTATION_QUALIFIED = "$CORE_PACKAGE_NAME.navargs.$NAV_TYPE_SERIALIZER_ANNOTATION"

val rootNavGraphType = Importable(
    "RootGraph",
    "$CORE_PACKAGE_NAME.annotation.RootGraph"
)
val rootNavGraphGenParams = RawNavGraphGenParams(
    annotationType = rootNavGraphType,
    isNavHostGraph = true,
    defaultTransitions = Importable(
        "NoTransitions",
        "com.ramcosta.composedestinations.animations.defaults.NoTransitions"
    ),
    deepLinks = emptyList(),
    sourceIds = emptyList(),
    navArgs = null,
    visibility = Visibility.PUBLIC,
    externalRoutes = emptyList()
)

const val DESTINATION_ANNOTATION_ROUTE_ARGUMENT = "route"
const val DESTINATION_ANNOTATION_STYLE_ARGUMENT = "style"
const val DESTINATION_ANNOTATION_WRAPPERS_ARGUMENT = "wrappers"
const val DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT = "navArgs"
const val DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT = "deepLinks"

const val DEEP_LINK_ANNOTATION_FULL_ROUTE_PLACEHOLDER = "@ramcosta.destinations.fullroute@" // Needs to be the same as the constant in core module
const val DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER = "@ramcosta.destinations.composable-name-route@" // Needs to be the same as the constant in core module's Destination
const val ACTIVITY_DESTINATION_ANNOTATION_DEFAULT_NULL = "@ramcosta.destinations.activity-null-default@" // Needs to be the same as the constant in core module's ActivityDestination
const val NAV_GRAPH_ANNOTATION_DEFAULT_NAME = "@ramcosta.destinations.annotation-navgraph-route@" // Needs to be the same as the constant in core module's NavGraph

const val GENERATED_NAV_GRAPHS_OBJECT = "NavGraphs"
const val GENERATED_DESTINATION_SUFFIX = "Destination"

const val CORE_ALIAS_DESTINATION_SPEC = "DestinationSpec"
const val CORE_TYPED_DESTINATION_SPEC = "TypedDestinationSpec"
const val CORE_DIRECTION_DESTINATION_SPEC = "DirectionDestinationSpec"
const val CORE_ALIAS_NAV_GRAPH_SPEC = "NavGraphSpec"

val CORE_NAV_HOST_ANIMATED_DESTINATION_STYLE = Importable("NavHostAnimatedDestinationStyle", "$CORE_PACKAGE_NAME.animations.NavHostAnimatedDestinationStyle")
val CORE_TYPED_NAV_HOST_GRAPH_SPEC = Importable("TypedNavHostGraphSpec", "$CORE_PACKAGE_NAME.spec.TypedNavHostGraphSpec")
val CORE_DIRECTION_NAV_HOST_GRAPH_SPEC = Importable("DirectionNavHostGraphSpec", "$CORE_PACKAGE_NAME.spec.DirectionNavHostGraphSpec")
val CORE_DIRECTION_NAV_GRAPH_SPEC = Importable("DirectionNavGraphSpec", "$CORE_PACKAGE_NAME.spec.DirectionNavGraphSpec")
val CORE_TYPED_NAV_GRAPH_SPEC = Importable("TypedNavGraphSpec", "$CORE_PACKAGE_NAME.spec.TypedNavGraphSpec")
val CORE_ACTIVITY_DESTINATION_SPEC = Importable("ActivityDestinationSpec", "$CORE_PACKAGE_NAME.spec.ActivityDestinationSpec")
val CORE_DIRECTION_ACTIVITY_DESTINATION_SPEC = Importable("DirectionActivityDestinationSpec", "$CORE_PACKAGE_NAME.spec.DirectionActivityDestinationSpec")

val CORE_STRING_NAV_TYPE = Importable("stringNavType", "$CORE_PACKAGE_NAME.navargs.primitives.stringNavType")
val CORE_INT_NAV_TYPE = Importable("intNavType", "$CORE_PACKAGE_NAME.navargs.primitives.intNavType")
val CORE_BOOLEAN_NAV_TYPE = Importable("booleanNavType", "$CORE_PACKAGE_NAME.navargs.primitives.booleanNavType")
val CORE_FLOAT_NAV_TYPE = Importable("floatNavType", "$CORE_PACKAGE_NAME.navargs.primitives.floatNavType")
val CORE_LONG_NAV_TYPE = Importable("longNavType", "$CORE_PACKAGE_NAME.navargs.primitives.longNavType")
val CORE_LONG_ARRAY_NAV_TYPE = Importable("longArrayNavType", "$CORE_PACKAGE_NAME.navargs.primitives.array.longArrayNavType")
val CORE_BOOLEAN_ARRAY_NAV_TYPE = Importable("booleanArrayNavType", "$CORE_PACKAGE_NAME.navargs.primitives.array.booleanArrayNavType")
val CORE_INT_ARRAY_NAV_TYPE = Importable("intArrayNavType", "$CORE_PACKAGE_NAME.navargs.primitives.array.intArrayNavType")
val CORE_FLOAT_ARRAY_NAV_TYPE = Importable("floatArrayNavType", "$CORE_PACKAGE_NAME.navargs.primitives.array.floatArrayNavType")
val CORE_STRING_ARRAY_NAV_TYPE = Importable("stringArrayNavType", "$CORE_PACKAGE_NAME.navargs.primitives.array.stringArrayNavType")
val CORE_BOOLEAN_ARRAY_LIST_NAV_TYPE = Importable("booleanArrayListNavType", "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.booleanArrayListNavType")
val CORE_FLOAT_ARRAY_LIST_NAV_TYPE = Importable("floatArrayListNavType", "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.floatArrayListNavType")
val CORE_INT_ARRAY_LIST_NAV_TYPE = Importable("intArrayListNavType", "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.intArrayListNavType")
val CORE_LONG_ARRAY_LIST_NAV_TYPE = Importable("longArrayListNavType", "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.longArrayListNavType")
val CORE_STRING_ARRAY_LIST_NAV_TYPE = Importable("stringArrayListNavType", "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.stringArrayListNavType")

const val CORE_DIRECTION = "Direction"
const val CORE_DESTINATION_ANIMATION_STYLE = "DestinationStyle.Animated"
const val CORE_BOTTOM_SHEET_DESTINATION_STYLE = "DestinationStyleBottomSheet"

private const val EXPERIMENTAL_ANIMATION_API_SIMPLE_NAME = "ExperimentalAnimationApi"
private const val EXPERIMENTAL_ANIMATION_API_QUALIFIED_NAME = "androidx.compose.animation.ExperimentalAnimationApi"
val experimentalAnimationApiType = Importable(
    simpleName = EXPERIMENTAL_ANIMATION_API_SIMPLE_NAME,
    qualifiedName = EXPERIMENTAL_ANIMATION_API_QUALIFIED_NAME
)

const val DESTINATIONS_NAVIGATOR_QUALIFIED_NAME = "$CORE_PACKAGE_NAME.navigation.DestinationsNavigator"
const val RESULT_BACK_NAVIGATOR_QUALIFIED_NAME = "$CORE_PACKAGE_NAME.result.ResultBackNavigator"
const val RESULT_RECIPIENT_QUALIFIED_NAME = "$CORE_PACKAGE_NAME.result.ResultRecipient"
const val OPEN_RESULT_RECIPIENT_QUALIFIED_NAME = "$CORE_PACKAGE_NAME.result.OpenResultRecipient"
const val NAV_CONTROLLER_QUALIFIED_NAME = "androidx.navigation.NavController"
const val NAV_HOST_CONTROLLER_QUALIFIED_NAME = "androidx.navigation.NavHostController"
val bundleImportable = Importable(
    simpleName = "Bundle",
    qualifiedName = "android.os.Bundle"
)
val savedStateHandleImportable = Importable(
    "SavedStateHandle",
    "androidx.lifecycle.SavedStateHandle"
)
const val NAV_BACK_STACK_ENTRY_SIMPLE_NAME = "NavBackStackEntry"
const val NAV_BACK_STACK_ENTRY_QUALIFIED_NAME = "androidx.navigation.$NAV_BACK_STACK_ENTRY_SIMPLE_NAME"
const val ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME = "AnimatedVisibilityScope"
const val ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME = "androidx.compose.animation.$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME"
const val COLUMN_SCOPE_SIMPLE_NAME = "ColumnScope"
const val COLUMN_SCOPE_QUALIFIED_NAME = "androidx.compose.foundation.layout.$COLUMN_SCOPE_SIMPLE_NAME"

const val BOTTOM_SHEET_DEPENDENCY = "io.github.raamcosta.compose-destinations:bottom-sheet"
