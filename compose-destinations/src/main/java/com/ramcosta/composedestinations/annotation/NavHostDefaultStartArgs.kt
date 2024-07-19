package com.ramcosta.composedestinations.annotation

/**
 * Annotation used on a public top level field to make it be used
 * as the start navigation arguments of a [NavHostGraph].
 * Useful when the start route of the [NavHostGraph] has mandatory
 * arguments and you don't want to make them non mandatory (because
 * maybe when you navigate to that destination, defaults make no sense,
 * they only make sense when the app starts initially).
 *
 * Example:
 * ```
 *  @NavHostDefaultStartArgs<RootGraph>
 *  val defaultRootGraphStartArgs = MyStartDestinationArgs(
 *      //fill default arg values here
 *  )
 *  ```
 *
 *  Note that `RootGraph` in the example is the [NavHostGraph] you want
 *  to use these on, and the `MyStartDestinationArgs` is the start route
 *  arguments class (which needs to match with the nav arguments class of
 *  the NavHost's start route).
 *
 *  @param T annotation annotated with [NavHostGraph] to which you want
 *  to se the default nav arguments to.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class NavHostDefaultStartArgs<T: Annotation>