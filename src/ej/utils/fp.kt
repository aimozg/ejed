package ej.utils

import org.funktionale.either.Either

/*
 * Created by aimozg on 14.10.2018.
 * Confidential until published on GitHub
 */
fun <L> L.iAmEitherLeft() = Either.left(this)

fun <R> R.iAmEitherRight() = Either.right(this)

@JvmName("invoke0AsLeft")
operator fun <L, R, RET> ((Either<L, R>) -> RET).invoke(left: L): RET =
		invoke(Either.left(left))

@JvmName("invoke0AsRight")
operator fun <L, R, RET> ((Either<L, R>) -> RET).invoke(right: R): RET =
		invoke(Either.right(right))

@JvmName("invoke1AsLeft")
operator fun <L, R, T1, RET> ((T1, Either<L, R>) -> RET).invoke(arg1: T1, left: L): RET =
		invoke(arg1, Either.left(left))

@JvmName("invoke1AsRight")
operator fun <L, R, T1, RET> ((T1, Either<L, R>) -> RET).invoke(arg1: T1, right: R): RET =
		invoke(arg1, Either.right(right))
