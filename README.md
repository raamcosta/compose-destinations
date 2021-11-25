[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)

# Compose Destinations

A KSP library to improve Compose Navigation. It processes annotations with KSP to generate code which uses
Compose Navigation under the hood. 
Doing so, it avoids the boilerplate and unsafe code around navigating in Compose.
For a deeper look into all the features, check our [wiki](https://github.com/raamcosta/compose-destinations/wiki).

## Table of contents

* [Usage](#usage)
* [Setup](#setup)
* [Current state](#current-state)
* [License](#license)

## Usage

1. Annotate your screen Composables with `@Destination`:

```kotlin
@Destination
@Composable
fun ProfileScreen() { /*...*/ }
```

2. Add navigation arguments to the function declaration:

```kotlin
@Destination
@Composable
fun ProfileScreen(
    id: Int, // <-- required navigation argument
    groupName: String?, // <-- optional navigation argument
    isOwnUser: Boolean = false // <-- optional navigation argument
) { /*...*/ }
```

> There is an alternative way to define the destination arguments in case you don't need to use them
inside the Composable (as is likely the case when using ViewModel). Read more [here](https://github.com/raamcosta/compose-destinations/wiki/Destination-arguments#navigation-arguments-class-delegate).

3. Build the project (or `./gradlew kspDebugKotlin`, which should be faster) to generate
all the Destinations. With the above annotated composable, a `ProfileScreenDestination` file (that we'll use on step 4) would be generated.

4. Use the generated `[ComposableName]Destination` invoke method to navigate to it. It will
have the correct typed arguments.

```kotlin
@Destination
@Composable
fun SomeOtherScreen(
    navigator: DestinationsNavigator
) {
    /*...*/
    navigator.navigate(ProfileScreenDestination(id = 7, groupName = "Kotlin programmers"))
}
```
> DestinationsNavigator is a wrapper interface to NavController that if declared as a parameter, will be provided for free by the library. NavController can also be provided in the exact same way, but it ties your composables to a specific implementation which will make it harder to test and preview. Read more [here](https://github.com/raamcosta/compose-destinations/wiki/Navigation) 

5. Finally, add the NavHost call:

```kotlin
DestinationsNavHost()
```
This call will automatically add all annotated Composable functions as destinations of the Navigation Graph.

That's it! No need to worry about routes, `NavType`, bundles and strings. All that redundant and
error-prone code gets generated for you.

## Setup

Compose destinations is available via maven central.

1. Add the ksp plugin:
```gradle
plugins {
    //...
    id("com.google.devtools.ksp") version "1.5.31-1.0.0"
}
```

2. Add the dependencies:
```gradle
implementation 'io.github.raamcosta.compose-destinations:core:0.9.4-beta'
ksp 'io.github.raamcosta.compose-destinations:ksp:0.9.4-beta'

// official compose navigation
implementation 'androidx.navigation:navigation-compose:$compose_navigation_version'
```
> Official Compose Navigation is required.
If you're using Accompanist Navigation-Animation and/or
Accompanist Material (aka BottomSheet, currently), Compose Destinations has you covered. <br/>
Check our [wiki](https://github.com/raamcosta/compose-destinations/wiki) to know more. <br/>
Each [release](https://github.com/raamcosta/compose-destinations/releases) contains a list of 
versions known to be compatible.

3. And finally, you need to make sure the IDE looks at the generated folder.
See KSP related [issue](https://github.com/google/ksp/issues/37).
An example for the debug variant would be:
```gradle
sourceSets {
    //...
    debug {
        java.srcDir(file("build/generated/ksp/debug/kotlin"))
    }
}
```

## Current state

The library is now in its beta stage, which means that I am happy
with the core feature set. If the APIs change, I will provide a migration path.
Please do try it and open issues if you find any.
If you're interested in contributing, I can give you a general overview of how the code works.
It is much simpler that what it might look like at first glance.

Any feedback and contributions are highly appreciated! 🙏

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
