package ej.utils

import org.funktionale.either.Either

/*
 * Created by aimozg on 14.10.2018.
 * Confidential until published on GitHub
 */
fun <L> L.iAmEitherLeft() = Either.left(this)

fun <R> R.iAmEitherRight() = Either.right(this)
