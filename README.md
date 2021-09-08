# Compose Destinations

#### What?

Compose destinations is a library to use alongside compose navigation. If you are already using compose navigation, then this will be a good and easy addition that
will make adding new destinations have less boilerplate and be less error prone as well as enabling type-safe navigation.

#### Why?

- How nice would it be if navigation compose did not envolve as much bundles, strings and other not type-safe stuff?
- What if you could simply add parameters to the `Composable` function to define the destination arguments?
- And what if you did not have to do anything else other than create the composable which composes the screen when you add a new screen? (Instead of remembering 
to add a `composable` call in the `NavHost`, and most likely adding the destination to some kind of `sealed class`)

If these seem interesting to you, then the why is already answered.

#### How?

1. Start by annotating the `Composable` functions that you want added as a navigation graph destination with `@Destination`.

```kotlin
@Destination(route = "home", start = true)
@Composable
fun HomeScreen(
    navController: NavController,
    scaffoldState: ScaffoldState
) {
	//...
}
```

2. Replace your `NavHost` call with `Destinations.NavHost` (or if using a `Scaffold`, then replace it with `Destinations.Scaffold`). 
You can also remove the builder blocks.

```kotlin
Destinations.NavHost(
    navController = rememberNavController()
)
```
OR
```kotlin
Destinations.Scaffold(
    scaffoldState = rememberScaffoldState(),
    navController = rememberNavController(),
)
```

3. If the destination has arguments, then simply add them as arguments to the `Composable` function!

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

Thats it! No messing with `NavType`, no messing with weird routes, bundles and strings. All this will be taken care for you.

Oh and by the way, what if the destination has default arguments? Wouldn't it be wonderful if we could just use Kotlin default parameters feature?
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

NOTE: Currently, arguments do have some limitations:
- They must be one of `String`, `Boolean`, `Int`, `Float`, `Long`, `NavController`, `NavBackStackEntry` or `ScaffoldState` (only if you are using `Scaffold`).
- All `Composable` function arguments will be considered navigation arguments except for `NavController`, `NavBackStackEntry` or `ScaffoldState`.
If you have a view model that you want to access with for example `hiltViewModel()`, it cannot be a Destination argument. The good practice is to call other
`Composable` function and use the Destination Composable as a wrapper to the testable screen `Composable`.
- Default values must be resolvable from the generated `DestinationSpec` class. The code that you write after that `=` will be exactly what is used inside the generated classes.
This means you won't be able to use a private constant as the default value.

These haven't been an issue in my samples, since usually arguments are simple things like IDs that you will use to access data from a repository of some sorts.
However, we'll be looking for ways to improve it.

#### Additional Features:

- All composables annotated will generate an implementation of `DestinationSpec` which is a sealed interface that contains the full route, navigation arguments,
`Content` composable method that will call your `Composable` and the `withArgs` implementation for that screen.
- `Scaffold` composable lambda parameters will be given a current `DestinationSpec`. This makes it trivial to update topbar, bottombar and drawer.
- Would you like to have additional fields in the `DestinationSpec`? Like for example a "title" which will be shown to the user for each screen? Since the 
`DestinationSpec` is a sealed interface, sealed `when` expressions will work very well with an extension field or function where you define it for each of your
destinations. (check sample app `DestinationSpecExtensions.kt`)
