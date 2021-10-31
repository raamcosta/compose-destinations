[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)

# Compose Destinations

A KSP library to improve Compose Navigation. It processes annotations with KSP to generate code which uses
Compose Navigation under the hood to make everything happen.
For a deeper look into all the features, check our [wiki](https://github.com/raamcosta/compose-destinations/wiki)(🚧).

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

2. Add navigation arguments to function declarations:

```kotlin
@Destination
@Composable
fun ProfileScreen(
    id: Int
) { /*...*/ }
```
Default values are allowed. Nullable values are also available for String values (limitation 
from Compose Navigation).
Both will become optional to navigate to this destination.

There is an alternative way to define the destination arguments in case you don't need to use them
inside the Composable (as is likely the case when using ViewModel). Read more [here](https://github.com/raamcosta/compose-destinations/wiki/Navigation#defining-navigation-arguments).

3. Use the generated `[ComposableName]Destination.withArgs` method to navigate to it:

```kotlin
@Destination
@Composable
fun SomeOtherScreen(
    navigator: DestinationsNavigator
) {
    /*...*/
    navigator.navigate(ProfileDestination.withArgs(id = 7))
}
```
You may need to build the project (or `./gradlew kspDebugKotlin`, which should be faster) to import
the generated Destinations (like the above `ProfileDestination`)

4. Finally, add the NavHost call:

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
implementation 'io.github.raamcosta.compose-destinations:core:0.9.0-beta'
ksp 'io.github.raamcosta.compose-destinations:ksp:0.9.0-beta'

// official compose navigation
implementation 'androidx.navigation:navigation-compose:$compose_navigation_version'
```
Official Compose Navigation is required.
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
    main {
        java.srcDir(file("build/generated/ksp/debug/kotlin"))
    }
}
```

## Current state

The library is now in its beta stage, which means that, for the most part, I am happy
with the core feature set, and if the APIs change, I will provide a migration path.
It might have some unknown bugs (and actually it's likely), but I'm confident that 
excluding some more exotic uses, the library is stable.
Still, I'd love to see people try to use it and opening issues if they find any.
Even though I am currently only one maintainer - _if you're interested in contributing
I can give you a general overview of how the code works_ - I plan to fix any bugs in
a timely manner and improve the stability even more going further.

Any feedback and contributions are highly appreciated!

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
