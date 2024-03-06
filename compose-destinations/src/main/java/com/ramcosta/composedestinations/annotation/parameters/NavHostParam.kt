package com.ramcosta.composedestinations.annotation.parameters

/**
 * Annotation that can be used in arguments of Destination Composable
 * functions to signal Compose Destinations to not consider them as
 * navigation arguments.
 * By default, all arguments with types Compose Destinations allows
 * you to pass as a navigation argument are considered navigation
 * arguments. So use this if you have an argument with such type
 * but you don't want to pass it from previous destination.
 *
 * There are usually two types of arguments you can set on Destination
 * annotated functions:
 * - Navigation arguments
 * - Parameters passed in from DestinationsNavHost level
 *
 * Arguments marked with this annotation, need to be passed in from
 * DestinationsNavHost.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class NavHostParam
