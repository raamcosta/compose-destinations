[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)

# Compose Destinations

Compose destinations is a KSP library to use alongside compose navigation. It makes managing destinations easier, requiring no boilerplate code and being less error-prone. Navigations between screen destinations are type-safe, since the APIs used for declaring/sending arguments will use the type system, while all the "non-type-safe" code required by compose navigation is generated for you.

## Why?

Because:
- How nice would it be if navigation compose did not rely as much on bundles, strings and other "non-type-safe" stuff?
- What if we could simply add parameters to the `Composable` function to define the destination arguments?
- And what if the navigation graph could be built/updated automatically when we add new or remove old screen composables?

## How?

1. Start by annotating the `Composable` functions that you want to add to the navigation graph with `@Destination`.

```kotlin
@Destination(route = "home", start = true)
@Composable
fun HomeScreen(
    navController: NavController
) {
	//...
}
```
NOTE: You can use `DestinationsNavigator` instead of `NavController` to make these Composables testable and "previewable". Read more in [Going deeper](#Going-deeper)

2. Replace your `NavHost` call with `Destinations.NavHost` (or if using a `Scaffold`, then replace it with `Destinations.Scaffold`). 
You can also remove the builder blocks, you won't be needing them anymore.

```kotlin
Destinations.NavHost(
    navController = myNavController
)
```
OR
```kotlin
Destinations.Scaffold(
    scaffoldState = myScaffoldState,
    navController = myNavController,
)
```
You don't even need to pass `NavHostController`/ `ScaffoldState` if you don't need to get a hold of them for some other reason.

3. If the destination has arguments, then simply add them to the `Composable` function!

```kotlin
@Destination(route = "user")
@Composable
fun UserScreen(
    userId: Long
)
```

Then, to navigate to the User Screen, anywhere you have the `navController`.

```kotlin
navController.navigate(UserScreenDestination.withArgs(userId = 1))
```

That's it! No messing with `NavType`, weird routes, bundles and strings. All this will be taken care for you.

Oh and by the way, what if the destination has default arguments? Wouldn't it be nice if we could just use Kotlin default parameters feature?
Well, that is exactly how we do it:

```kotlin
@Destination(route = "user")
@Composable
fun UserScreen(
    userId: Long,
    isOwnUser: Boolean = false
)
```

Now the IDE will even tell you the default arguments of the composable when calling the `withArgs` method!

Notes about arguments:
- They must be one of `String`, `Boolean`, `Int`, `Float`, `Long` to be considered navigation arguments.
  `NavController`, `DestinationsNavigator`, `NavBackStackEntry` or `ScaffoldState` (only if you are using `Scaffold`) can also be used by all destinations.
- Navigation arguments' default values must be resolvable from the generated `Destination` class since the code written after the "`=`" 
  will be copied into it as is. 
Unfortunately, this means you won't be able to use a constant or a function call as the default value of a nav argument. However, if the parameter 
  type is not a navigation argument type, then everything is valid since it won't be considered a navigation argument of the destination.
  For example:
```kotlin
@Destination(route = "greeting", start = true)
@Composable
fun Greeting(
    navigator: DestinationsNavigator,
    coroutineScope: CoroutineScope = rememberCoroutineScope() //valid because CoroutineScope is not a navigation argument type
)

@Destination(route = "user")
@Composable
fun UserScreen(
    navigator: DestinationsNavigator,
    id: Int = getDefaultUserId() //not valid because Int is a navigation argument type so we need to resolve the default value in the generated classes
)

//As a temporary workaround, you could define the argument as nullable (or lets say -1)
@Destination(route = "user")
@Composable
fun UserScreen(
  navigator: DestinationsNavigator,
  id: Int? = null
) {
  //then here do:
  val actualId = id ?: getDefaultUserId()
}
```
We'll be looking for ways to improve this.

## Deep Linking

You can define deeps links to a destination like this:

```kotlin
@Destination(
  route = "user",
  deepLinks = [
    DeepLink(
      uriPattern = "https://myapp.com/user/{id}"
    )
  ]
)
@Composable
fun UserScreen(
  navigator: DestinationsNavigator,
  id: Int
)
```
You can also use the placeholder suffix `FULL_ROUTE_PLACEHOLDER` in your `uriPattern`. In the code generation process it will be replaced with the full route of the destination which contains all the destination arguments. So, for example, this would result in the same `uriPattern` as the above example:
```kotlin
@Destination(
  route = "user",
  deepLinks = [
    DeepLink(
      uriPattern = "https://myapp.com/$FULL_ROUTE_PLACEHOLDER"
    )
  ]
)
@Composable
fun UserScreen(
  navigator: DestinationsNavigator,
  id: Int
)
```

## Dependencies

Compose destinations is available via maven central.

1. Add the ksp plugin:
```gradle
plugins {
    //...
    id("com.google.devtools.ksp") version "1.5.21-1.0.0-beta07" // This will change to the stable ksp version when compose allows us to use kotlin 1.5.30
}
```

2. Add the dependencies:
```gradle
implementation 'io.github.raamcosta.compose-destinations:core:0.6.2-alpha03'
ksp 'io.github.raamcosta.compose-destinations:ksp:0.6.2-alpha03'

```

3. And finally, you need to make sure the IDE looks at the generated folder.
See KSP related [issue](https://github.com/google/ksp/issues/37).
An example for the debug variant would be:
```gradle
sourceSets {
    //...
    main {
        java.srcDir(file("build/generated/ksp/debug/kotlin"))
    }
}
```

## Going deeper

- It is good practice to not depend directly on `NavController` on your Composeables. You can choose to depend on `DestinationsNavigator` instead of `NavController`, which is an interface wrapper of `NavController` that allows to easily pass an empty implementation (one is available already `EmptyDestinationsNavigator`) for
previews or testing. All above examples can replace `navController: NavController` with `navigator: DestinationsNavigator`, in order to make use of
this dependency inversion principle.
- All annotated composables will generate an implementation of `Destination` which is a sealed interface that contains the full route, navigation arguments,
  `Content` composable function and the `withArgs` implementation.
- `Destination` annotation can receive a `navGraph` parameter for nested navigation graphs. This will be the route of the nested graph and all destinations with the same `navGraph` will belong to it. If this parameter is not specified, then the `Destination` will belong to the root navigation graph (which is the norm when not using nested nav graphs)
- `Scaffold` composable lambda parameters will be given a current `Destination`. This makes it trivial to have top bar, bottom bar and drawer depend on the current destination.
- Besides the `NavHost` and `Scaffold` wrappers, the generated `Destinations` class contains all `NavGraphs`. Each `NavGraph` contains the start `Destination` as well as all its destinations and its nested `NavGraphs`.
- If you would like to have additional properties/functions in the `Destination` (for example a "title" which will be shown to the user for each screen) you can make an extension
  property/function of `Destination` for a similar effect. Since it is a sealed interface, a `when` expression will make sure you always have a definition for each screen (check this file for an example [file](https://github.com/raamcosta/compose-destinations/blob/main/app/src/main/java/com/ramcosta/samples/destinationstodosample/DestinationSpecExtensions.kt)).

## Current state

This lib is still in its alpha stage, APIs can change.
I'm looking for all kinds of feedback, issues, feature requests and help in improving the code or even this README. So please, if you find this interesting, try it out in
some sample projects and let me know how it goes!

## License

    Copyright 2021 Rafael Costa

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
