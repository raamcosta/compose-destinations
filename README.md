[![Maven metadata URL](https://img.shields.io/maven-metadata/v?color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)
[![License Apache 2.0](https://img.shields.io/github/license/raamcosta/compose-destinations.svg?style=for-the-badge&color=orange)](https://opensource.org/licenses/Apache-2.0)
[![Android API](https://img.shields.io/badge/api-21%2B-brightgreen.svg?style=for-the-badge)](https://android-arsenal.com/api?level=21)
[![kotlin](https://img.shields.io/github/languages/top/raamcosta/compose-destinations.svg?style=for-the-badge&color=blueviolet)](https://kotlinlang.org/)

<p align="center"> 
   <img height="250" src="https://user-images.githubusercontent.com/80427734/147891822-5cd34c80-8dca-4d34-8278-2aa3bf36913f.png"/> 
</p>

<h1 align="center"> Compose Destinations </h1>

A KSP library that processes annotations and generates code that uses Official Jetpack Compose Navigation under the hood. It hides the complex, non-type-safe and boilerplate code you would have to write otherwise. </br>
No need to learn a whole new framework to navigate - most APIs are either the same as with the Jetpack Components or inspired by them.

- Main features:
    - Typesafe navigation arguments
    - Simple but configurable navigation graphs setup 
    - Navigating back with a result in a simple and type-safe way
    - Getting the navigation arguments from the `SavedStateHandle` (useful in ViewModels) and `NavBackStackEntry` in a type-safe way.
    - Navigation animations through integration with [Accompanist Navigation-Animation](https://github.com/google/accompanist/tree/main/navigation-animation)
    - Bottom sheet screens through integration with [Accompanist Navigation-Material](https://github.com/google/accompanist/tree/main/navigation-material)
    - Easy deep linking to screens
    - All you can do with Official Jetpack Compose Navigation but in a simpler safer way!

For a deeper look into all the features, check our [wiki](https://github.com/raamcosta/compose-destinations/wiki).

## Usage

1. Annotate your screen Composables with `@Destination`:

```kotlin
@Destination
@Composable
fun ProfileScreen() { /*...*/ }
```

2. Add navigation arguments to the function declaration: </br>
(`Parcelable`, `Serializable` and `Enum` types are allowed!)

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
   all the Destinations. With the above annotated composable, a `ProfileScreenDestination` file (that we'll use in step 4) would be generated.

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
DestinationsNavHost(navGraph = NavGraphs.root)
```
> `NavGraphs` is a generated file that describes your navigation graphs and their destinations. By default all destinations will belong to "root", but you can use the `navGraph` argument of the annotation to have certain screens in nested navigation graphs.

This call adds all annotated Composable functions as destinations of the Navigation Host.

That's it! No need to worry about routes, `NavType`, bundles and strings. All that redundant and
error-prone code gets generated for you.

## Setup

Compose destinations is available via maven central.

#### 1. Add the KSP plugin:

> The version you chose for the KSP plugin depends on the Kotlin version your project uses. </br>
You can check https://github.com/google/ksp/releases for the list of KSP versions, then pick the last release that matches your Kotlin version.
Example:
If you're using `1.5.31` Kotlin version, then the last KSP version is `1.5.31-1.0.1`.

<details open>
  <summary>groovy - build.gradle(:app)</summary>

```gradle
plugins {
    //...
    id 'com.google.devtools.ksp' version '1.5.31-1.0.1' // Depends on your kotlin version
}
```
</details>

<details>
  <summary>kotlin - build.gradle.kts(:app)</summary>  

```gradle
plugins {
    //...
    id("com.google.devtools.ksp") version "1.5.31-1.0.1" // Depends on your kotlin version
}
```
</details>


#### 2. Add the dependencies:

<details open>
  <summary>groovy - build.gradle(:app)</summary>

```gradle
implementation 'io.github.raamcosta.compose-destinations:core:1.2.2-beta'
ksp 'io.github.raamcosta.compose-destinations:ksp:1.2.2-beta'    
```
</details>

<details>
  <summary>kotlin - build.gradle.kts(:app)</summary>  

```gradle
implementation("io.github.raamcosta.compose-destinations:core:1.2.2-beta")
ksp("io.github.raamcosta.compose-destinations:ksp:1.2.2-beta")
```
</details>

> If you want to use animations between screens and/or bottom sheet screens, replace above core dependency with: </br>
`implementation 'io.github.raamcosta.compose-destinations:animations-core:<version>'` </br>
> this will use [Accompanist Navigation-Animation](https://github.com/google/accompanist/tree/main/navigation-animation) and [Accompanist Navigation-Material](https://github.com/google/accompanist/tree/main/navigation-material) internally. </br>
> Read more about the next steps to configure these features [here](https://github.com/raamcosta/compose-destinations/wiki/Styles-and-Animations)


#### 3. And finally, you need to make sure the IDE looks at the generated folder.
See KSP related [issue](https://github.com/google/ksp/issues/37).
An example for the debug/release variant would be:

groovy/kotlin - gradle.build(:app) (same level as `plugins` and `android` blocks):
```gradle
kotlin {
    sourceSets {
        debug {
            kotlin.srcDir("build/generated/ksp/debug/kotlin")
        }
        release {
            kotlin.srcDir("build/generated/ksp/release/kotlin")
        }
    }
}
```

## Current state

The library is now in its beta stage, which means that I am happy
with the core feature set. If the APIs change, I will provide a migration path.
Please do try it and open issues if you find any.
If you're interested in contributing, I can give you a general overview of how the code works.
It is much simpler than what it might look like at first glance.

Any feedback and contributions are highly appreciated! 🙏
