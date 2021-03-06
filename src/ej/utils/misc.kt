package ej.utils

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

fun <T:Comparable<R>,R> T.lessThan(other:R) = this < other
fun <T:Comparable<R>,R> T.lessThanOrEqualTo(other:R) = this <= other
fun <T:Comparable<R>,R> T.greaterThan(other:R) = this > other
fun <T:Comparable<R>,R> T.greaterThanOrEqualTo(other:R) = this >= other

fun Boolean.toString(ifTrue: String, ifFalse: String) = if (this) ifTrue else ifFalse
fun <T> Boolean.trueFalse(ifTrue: T, ifFalse: T) = if (this) ifTrue else ifFalse
