[![Maven metadata URL](https://img.shields.io/maven-metadata/v?versionPrefix=2.2&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)
[![License Apache 2.0](https://img.shields.io/github/license/raamcosta/compose-destinations.svg?style=for-the-badge&color=orange)](https://opensource.org/licenses/Apache-2.0)
[![Android API](https://img.shields.io/badge/api-21%2B-brightgreen.svg?style=for-the-badge)](https://android-arsenal.com/api?level=21)
[![kotlin](https://img.shields.io/github/languages/top/raamcosta/compose-destinations.svg?style=for-the-badge&color=blueviolet)](https://kotlinlang.org/)

<p align="center">
   <img height="500" src="/.idea/icon.svg"/>  
</p>

<h1 align="center"> 
   <a href="https://composedestinations.rafaelcosta.xyz">Compose Destinations</a>
</h1>

A KSP library that processes annotations and generates code that uses Official Jetpack Compose Navigation under the hood. It hides the complex, non-type-safe and boilerplate code you would have to write otherwise. </br>
No need to learn a whole new framework to navigate - most APIs are either the same as with the Jetpack Components or inspired by them.

## V2 is here! 🙌
Please consider migrating to it and leaving feedback as GH issue or on our slack channel [#compose-destinations](https://kotlinlang.slack.com/archives/C06CS4UCQ10)!  
 * Migration guide: https://composedestinations.rafaelcosta.xyz/migrating-to-v2
 * V2 docs: https://composedestinations.rafaelcosta.xyz/v2


## Main features 🧭
- Typesafe navigation arguments
- Simple but configurable navigation graphs setup
- Navigating back with a result in a simple and type-safe way
- Getting the navigation arguments from the `SavedStateHandle` (useful in ViewModels) and `NavBackStackEntry` in a type-safe way.
- Navigation animations
- Destination wrappers to allow reusing Compose logic on multiple screens
- Bottom sheet screens
- Easy deep linking to screens
- Wear OS support (since versions 1.x.30!)
- All you can do with Official Jetpack Compose Navigation but in a simpler safer way!

For a deeper look into all the features, check our [documentation website](https://composedestinations.rafaelcosta.xyz).

## Basic Usage 🧑‍💻

> [!NOTE]  
> This readme is about v2. If you're now starting to use Compose Destinations, I strongly recommend using v2.
> If you really want to see basic v1 usage, [check it here](https://composedestinations.rafaelcosta.xyz/#basic-usage). 

### 1. Annotate your screen Composables with `@Destination<RootGraph>`:

```kotlin
@Destination<RootGraph> // sets this as a destination of the "root" nav graph
@Composable
fun ProfileScreen() { /*...*/ }
```

### 2. Add navigation arguments to the function declaration:

```kotlin
@Destination<RootGraph>
@Composable
fun ProfileScreen(
   id: Int, // <-- required navigation argument
   groupName: String?, // <-- optional navigation argument
   isOwnUser: Boolean = false // <-- optional navigation argument
) { /*...*/ }
```

`Parcelable`, `Serializable`, `Enum` and classes annotated with [`@kotlinx.serialization.Serializable`](https://github.com/Kotlin/kotlinx.serialization) (as well as `Array`s and `ArrayList`s of these) work out of the box!
You can also make any other type a navigation argument type. Read about it [here](https://composedestinations.rafaelcosta.xyz/v2/arguments/navigation-arguments#custom-navigation-argument-types)

> [!TIP]  
> There is an alternative way to define the destination arguments in case you don't need to use them
inside the Composable (as is likely the case when using ViewModel). Read more [here](https://composedestinations.rafaelcosta.xyz/v2/arguments/navigation-arguments#destination-navigation-arguments).

### 3. Build the project
   Or run ksp task (example: `./gradlew kspDebugKotlin`), to generate all the Destinations. With the above annotated composable, a `ProfileScreenDestination` file would be generated (that we'll use in step 4).

### 4. Use the generated `[ComposableName]Destination`'s invoke method to navigate to it.
   It will have the correct typed arguments.

```kotlin
@Destination<RootGraph>(start = true) // sets this as the start destination of the "root" nav graph
@Composable
fun HomeScreen(
   navigator: DestinationsNavigator
) {
   /*...*/
   navigator.navigate(ProfileScreenDestination(id = 7, groupName = "Kotlin programmers"))
}
```

### 5. Finally, add the NavHost call:

```kotlin
DestinationsNavHost(navGraph = NavGraphs.root)
```
> [!NOTE]  
> `NavGraphs` is a generated file that contains all navigation graphs. 
> `root` here corresponds to the `<RootGraph>` we used in the above examples.
> You're also able to [define your own navigation graphs](https://composedestinations.rafaelcosta.xyz/v2/defining-navgraphs) to use instead of `<RootGraph>`.

This call adds all annotated Composable functions as destinations of the Navigation Host.

That's it! No need to worry about routes, `NavType`, bundles and strings. All that redundant and
error-prone code gets generated for you.

## Setup 🧩

Compose destinations is available via maven central.

#### 1. Add the KSP plugin:

> **Note**: The version you chose for the KSP plugin depends on the Kotlin version your project uses. </br>
You can check https://github.com/google/ksp/releases for the list of KSP versions, then pick the last release that matches your Kotlin version.
Example:
If you're using `1.9.22` Kotlin version, then the last KSP version is `1.9.22-1.0.17`.

<details open>
  <summary>groovy - build.gradle(:module-name)</summary>

```gradle
plugins {
    //...
    id 'com.google.devtools.ksp' version '1.9.22-1.0.17' // Depends on your kotlin version
}
```
</details>

<details>
  <summary>kotlin - build.gradle.kts(:module-name)</summary>  

```gradle
plugins {
    //...
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" // Depends on your kotlin version
}
```
</details>

#### 2. Add the dependencies:

Compose Destinations has multiple active versions. 
The higher one uses the latest versions for Compose and Navigation, while the others use only stable versions.
Choose the one that matches your Compose version, considering this table:

<table>
 <tr>
  <td>Compose 1.1 (1.1.x)</td><td><img alt="Maven Central" src="https://img.shields.io/maven-metadata/v?versionPrefix=1.5&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)"></td>
 </tr>
 <tr>
  <td>Compose 1.2 (1.2.x)</td><td><img alt="Maven Central" src="https://img.shields.io/maven-metadata/v?versionPrefix=1.6&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)"></td>
 </tr>
 <tr>
  <td>Compose 1.3 (1.3.x)</td><td><img alt="Maven Central" src="https://img.shields.io/maven-metadata/v?versionPrefix=1.7&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)"></td>
 </tr>
 <tr>
  <td>Compose 1.4 (1.4.x)</td><td><img alt="Maven Central" src="https://img.shields.io/maven-metadata/v?versionPrefix=1.8&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)"></td>
 </tr>
 <tr>
  <td>Compose 1.5 (1.5.x)</td><td><img alt="Maven Central" src="https://img.shields.io/maven-metadata/v?versionPrefix=1.9&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)"></td>
 </tr>
 <tr>
  <td>Compose 1.6 (1.6.x)</td>
    <td>
        <img alt="Maven Central" src="https://img.shields.io/maven-metadata/v?versionPrefix=1.10&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)">
        OR
        <img alt="Maven Central" src="https://img.shields.io/maven-metadata/v?versionPrefix=2.0&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)">
    </td>
 </tr>
 <tr>
  <td>Compose 1.7 (1.7.x)</td>
    <td>
        <img alt="Maven Central" src="https://img.shields.io/maven-metadata/v?versionPrefix=1.11&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)">
        OR
        <img alt="Maven Central" src="https://img.shields.io/maven-metadata/v?versionPrefix=2.1&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)">
    </td>
 </tr>
 <tr>
  <td>Compose 1.8 (1.8.x)</td>
    <td>
        <img alt="Maven Central" src="https://img.shields.io/maven-metadata/v?versionPrefix=2.2&color=blue&metadataUrl=https://s01.oss.sonatype.org/service/local/repo_groups/public/content/io/github/raamcosta/compose-destinations/core/maven-metadata.xml&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.github.raamcosta.compose-destinations/core)">
    </td>
 </tr>
</table>

> [!WARNING]  
> If you choose a version that uses a higher version of Compose than the one you're setting for your app, gradle will upgrade your Compose version via transitive dependency.

<details open>
  <summary>groovy - build.gradle(:module-name)</summary>

```gradle
implementation 'io.github.raamcosta.compose-destinations:core:<version>'
ksp 'io.github.raamcosta.compose-destinations:ksp:<version>'

// V2 only: for bottom sheet destination support, also add
implementation 'io.github.raamcosta.compose-destinations:bottom-sheet:<version>'
```
</details>

<details>
  <summary>kotlin - build.gradle.kts(:module-name)</summary>  

```gradle
implementation("io.github.raamcosta.compose-destinations:core:<version>")
ksp("io.github.raamcosta.compose-destinations:ksp:<version>")

// V2 only: for bottom sheet destination support, also add
implementation("io.github.raamcosta.compose-destinations:bottom-sheet:<version>")
```
</details>

> [!NOTE]  
> If you want to use Compose Destinations in a **Wear OS** app, replace above core dependency with: </br>
`implementation 'io.github.raamcosta.compose-destinations:wear-core:<version>'` </br>
> this will use [Wear Compose Navigation](https://developer.android.com/training/wearables/compose/navigation) internally. </br>
> Read more about the next steps to configure these features [here](https://composedestinations.rafaelcosta.xyz/wear-os)

# ⚠️ WARNING - `1.11.3-alpha` / `2.1.0-beta02` and above (Compose 1.7)
OR if you got this error `kotlinx.serialization.SerializationException: Serializer for class 'DirectionImpl' is not found.`


## - **DO NOT call `NavController.navigate` function** anywhere
With the introduction of type safe APIs on the official library, our `NavController` extension functions that received `Direction` are now shadowed by new member functions on `NavController`.  
This means that the official member function would be called instead of our extension functions, and so we removed those extension functions.  
Instead, always make sure to use `DestinationsNavigator`. You can get one of such navigators by:

- If inside a specific screen:
   - Simply receive a `DestinationsNavigator` instead of `NavController` in your annotated screens.
- If navigating on top level (such as around `DestinationsNavHost`, bottom nav bar, etc)
   - `navController.rememberDestinationsNavigator()` if in a Composable
   - `navController.toDestinationsNavigator()` if not in a Composable

Read more about these changes [here](https://github.com/raamcosta/compose-destinations/releases/tag/2.1.0-beta02).

## - **DO NOT depend on jetpack compose navigation directly**
Compose Destinations provides the correct version transitively.  
So, if you have dependency on `androidx.navigation:navigation-compose`, please remove it! This has always been true, but more important now.

## Community 💬

Please join the community at Kotlin slack channel: [#compose-destinations](https://kotlinlang.slack.com/archives/C06CS4UCQ10)  
Ask questions, suggest improvements, or anything else related to the library.


**If you like the library, consider starring and sharing it with your colleagues.**
