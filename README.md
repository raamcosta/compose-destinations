[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)

# Compose Destinations

A KSP library to improve Compose Navigation. It uses KSP to generate some code which uses
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

2. Simply add navigation arguments to the function declarations:

```kotlin
@Destination
@Composable
fun ProfileScreen(
    id: Int
) { /*...*/ }
```
Default values are also allowed. They will become optional to navigate to this destination.

3. Use the generated `[ComposableName]Destination.withArgs` method to navigate to them:

```kotlin
@Destination
@Composable
fun SomeOtherScreen(
    navigator: DestinationsNavigator
) {
    /*...*/
    navigator.navigate(ProfileScreenDestination.withArgs(id = 7))
}
```
You may need to build the project so that you can import the generated Destinations (like the above `ProfileScreenDestination`)

4. Finally, add the NavHost somewhere:

```kotlin
DestinationsNavHost()
```
This call will automatically add all annotated Composable functions as destinations of the Navigation Graph.

That's it! No need to worry about routes, `NavType`, bundles and strings. All that redundant and error-prone code gets generated for you.

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
implementation 'io.github.raamcosta.compose-destinations:core:0.8.4-alpha05'
ksp 'io.github.raamcosta.compose-destinations:ksp:0.8.4-alpha05'

// official compose navigation
implementation 'androidx.navigation:navigation-compose:$compose_navigation_version'
```
Official Compose Navigation is required.
If you're using Compose Material, Accompanist Navigation-Animation and/or
Accompanist Material (aka BottomSheet, currently), Compose Destinations has you covered. <br/>
Check our [wiki](https://github.com/raamcosta/compose-destinations/wiki) to know more. <br/>
Each [release](https://github.com/raamcosta/compose-destinations/releases) contains a list of versions known to be compatible.

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

This lib is still in its alpha stage, APIs can change.
I'm looking for all kinds of feedback, issues, feature requests and help in improving the code. So please, if you find this interesting, try it out in
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
